package tripster.tripster.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Response;
import com.couchbase.lite.Document;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import net.grandcentrix.tray.AppPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static tripster.tripster.Constants.CURR_TRIP_ID;
import static tripster.tripster.Constants.CURR_TRIP_LL;
import static tripster.tripster.Constants.CURR_TRIP_ST;
import static tripster.tripster.Constants.MY_ID;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;
import static tripster.tripster.Constants.PLACE_TIME_K;
import static tripster.tripster.Constants.PLACE_TRIP_K;
import static tripster.tripster.Constants.TRIP_LEVEL_K;
import static tripster.tripster.Constants.LEVEL_PRIVATE;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PAUSED;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_RUNNING;
import static tripster.tripster.Constants.TRIP_STOPPED_AT_K;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks {
  private static final String TAG = LocationService.class.getName();

  private static final int TRACKING_FREQUENCY = 5000; //ms
  private static final int MIN_DIST = 10; //meters

  private GoogleApiClient googleClient;
  private Timer timer;
  private AppPreferences pref;
  private TripsterDb tDb;

  private String currTripId;
  private String currTripSt;

  public LocationService() {
    super();
    googleClient = null;
    Log.i(TAG, "LocationService created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    pref = new AppPreferences(getApplicationContext());
    tDb = TripsterDb.getInstance(getApplicationContext());
    tDb.startPushSync(); // start push replication

    String userId = pref.getString(MY_ID, "");

    if (intent != null) {
      switch (intent.getStringExtra("flag")) {
        case "resume":
          resumeTrip();
          return START_STICKY;
        case "start":
          startTrip(userId);
          return START_STICKY;
        case "pause":
          pauseTrip();
          return START_NOT_STICKY;
        case "stop":
          stopTrip();
          return START_NOT_STICKY;
        default:
          return START_NOT_STICKY;
      }
    } else {
      Log.d(TAG, "No intent got; this must be a restart after app force closed me, so resume.");
      resumeTrip();
      return START_STICKY;
    }
  }

  private void setCurrentTripDetails(String running, String tripId) {
    currTripId = tripId;
    currTripSt = running;
    pref.put(CURR_TRIP_ID, tripId);
    pref.put(CURR_TRIP_ST, running);
  }

  private String getStatus() {
    if (currTripSt == null) {
      currTripSt = pref.getString(CURR_TRIP_ST, "");
    }
    return currTripSt;
  }

  private String getTripId() {
    if (currTripId == null) {
      currTripId = pref.getString(CURR_TRIP_ID, "");
    }
    return currTripId;
  }

  private void startTrip(String userId) {
    if (getStatus().isEmpty()) {
      String newId = UUID.randomUUID().toString();
      Map<String, Object> props = new HashMap<>();
      props.put(TRIP_OWNER_K, userId);
      props.put(TRIP_LEVEL_K, LEVEL_PRIVATE);
      tDb.upsertNewDocById(newId, props);
      setCurrentTripDetails(TRIP_RUNNING, newId); // to notify the activity

      connectGoogleClient(); // actualy start service
      Log.d(TAG, "Successfully started by app");
    } else {
      Log.e(TAG, "Unsuccessfully started by app");
    }
  }

  private void resumeTrip() {
    if (!getTripId().isEmpty()) {
      if (getStatus().equals(TRIP_PAUSED)) {
        setCurrentTripDetails(TRIP_RUNNING, getTripId());
      }

      connectGoogleClient();
      Log.d(TAG, "Resumed by app");
    } else {
      Log.e(TAG, "Unsuccessfully resumed by app");
    }
  }

  private void pauseTrip() {
    if (!getTripId().isEmpty() && getStatus().equals(TRIP_RUNNING)) {
      setCurrentTripDetails(TRIP_PAUSED, getTripId());

      stopMonitor();
      Log.d(TAG, "Paused by app");
    } else {
      Log.e(TAG, "Unsuccessfully paused by app");
    }
  }

  private void stopTrip() {
    if (!getTripId().isEmpty()) {
      Map<String, Object> props = new HashMap<>();
      props.put(TRIP_STOPPED_AT_K, System.currentTimeMillis());
      tDb.upsertNewDocById(getTripId(), props);
      Document currTrip = tDb.getDocumentById(getTripId());
      dumpTrip(currTrip);

      setCurrentTripDetails("", "");
      pref.put(CURR_TRIP_LL, "");

      Log.d(TAG, "Stopped by app");
      exit();
    } else {
      Log.e(TAG, "Unsuccessfully stopped by app");
    }
  }

  private void dumpTrip(Document t) {
    String s = t.getProperty(TRIP_NAME_K)
        + " with preview: " + t.getProperty(TRIP_PREVIEW_K)
        + " by: " + t.getProperty(TRIP_OWNER_K)
        + " was stopped at:" + new Date((long) t.getProperty(TRIP_STOPPED_AT_K)).toString();
    Log.d(TAG, "We just ended the following trip: " + s);
  }

  private String status = "initial";

  private void exit() {
    stopMonitor();
    status = "running";
    tDb.pushChanges(new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        status = response;
      }
    });
    while (status.equals("running"));
    Log.d(TAG, "closing service");
    stopSelf();
  }

  private void stopMonitor() {
    getTimer().cancel();
    timer = null;
    if (googleClient != null) {
      googleClient.disconnect();
    }
  }

  private Timer getTimer() {
    if (timer == null) {
      timer = new Timer();
    }
    return timer;
  }

  private synchronized void connectGoogleClient() {
    if (googleClient == null) {
      googleClient = new GoogleApiClient.Builder(this)
          .addApi(LocationServices.API)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
              Log.e(TAG, "Connection Failed");
            }
          }).build();
    }
    if (!googleClient.isConnected()) {
      googleClient.connect();
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.d("On connected", "Entered");
    TimerTask locationTimerTask = new TimerTask() {
      @Override
      public void run() {
        Log.d(TAG, "LocationTask is running");
        if (ActivityCompat.checkSelfPermission(LocationService.this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(LocationService.this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
          Log.e(TAG, "NO PERMISSIONS FOR LOCATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
          return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
        if (currentLocation != null) {
          addLocation(currentLocation);
        } else {
          Log.w(TAG, "Current location is null");
        }
      }
    };

    //schedule the timer, to wake up every TRACKING_FREQUENCY seconds
    getTimer().schedule(locationTimerTask, 0, TRACKING_FREQUENCY);
  }

  private void addLocation(Location currentLocation) {
    Document previousLocation = tDb.getDocumentById(pref.getString(CURR_TRIP_LL, ""));
    if (previousLocation != null) {
      Location prevLoc = new Location("");
      prevLoc.setLatitude((double) previousLocation.getProperty(PLACE_LAT_K));
      prevLoc.setLongitude((double) previousLocation.getProperty(PLACE_LNG_K));
      if (prevLoc.distanceTo(currentLocation) > MIN_DIST) {
        saveLocation(currentLocation);
      }
    } else {
      Log.d(TAG, "Saving Location");
      saveLocation(currentLocation);
    }
  }

  private void saveLocation(Location currentLocation) {
    String newId = UUID.randomUUID().toString();
    Map<String, Object> props = new HashMap<>();
    Log.d(TAG, "New location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + " at " + currentLocation.getTime());
    props.put(PLACE_LAT_K, currentLocation.getLatitude());
    props.put(PLACE_LNG_K, currentLocation.getLongitude());
    props.put(PLACE_TIME_K, currentLocation.getTime());
    props.put(PLACE_TRIP_K, getTripId());
    tDb.upsertNewDocById(newId, props);
    pref.put(CURR_TRIP_LL, newId);
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.e(TAG, "Connection Suspended " + i);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}