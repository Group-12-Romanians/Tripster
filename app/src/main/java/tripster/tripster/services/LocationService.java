package tripster.tripster.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Response;
import com.couchbase.lite.Document;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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
import static tripster.tripster.Constants.APP_NAME;
import static tripster.tripster.Constants.CURR_TRIP;
import static tripster.tripster.Constants.MY_ID;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_STOPPED_AT;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks {
  private static final String TAG = LocationService.class.getName();

  private static final int TRACKING_FREQUENCY = 1000; //ms
  private static final int MIN_DIST = 50; //meters
  private static final String DEFAULT_PREVIEW = "https://cdn1.tekrevue.com/wp-content/uploads/2015/04/map-location-pin.jpg";
  private static final String DEFAULT_NAME = "Current Trip";

  private GoogleApiClient googleClient;
  private Timer timer;
  private Pair<String, String> currentTripDetails = new Pair<>("", "");
  private String userId = "";
  private TripsterDb tDb;

  public LocationService() {
    super();
    googleClient = null;
    tDb = new TripsterDb(getApplicationContext());
    Log.i(TAG, "LocationService created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    userId = getSharedPreferences(APP_NAME, MODE_PRIVATE).getString(MY_ID, "");
    currentTripDetails = getCurrentTripDetails();

    tDb.startPushSync(); // start push replication
    if (intent != null) {
      switch (intent.getStringExtra("flag")) {
        case "resume":
          resumeTrip();
          return START_STICKY;
        case "start":
          startTrip();
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
    currentTripDetails = new Pair<>(running, tripId);
    getSharedPreferences(APP_NAME, MODE_PRIVATE).edit().putString(CURR_TRIP,
        currentTripDetails.first + ":" + currentTripDetails.second).apply();
  }

  private Pair<String, String> getCurrentTripDetails() {
    String prefDetails = getSharedPreferences(APP_NAME, MODE_PRIVATE).getString(CURR_TRIP, "");
    String[] parts = prefDetails.split(":");
    if (parts.length == 2) {
      return new Pair<>(parts[0], parts[1]);
    } else {
      throw new RuntimeException("Inconsistent current trip details:" + prefDetails);
    }
  }

  private String getStatus() {
    return currentTripDetails.first;
  }

  private String getTripId() {
    return currentTripDetails.second;
  }

  private void startTrip() {
    if (getStatus().isEmpty()) {
      String newId = UUID.randomUUID().toString();
      Map<String, Object> props = new HashMap<>();
      props.put(TRIP_NAME_K, DEFAULT_NAME);
      props.put(TRIP_PREVIEW_K, DEFAULT_PREVIEW);
      props.put(TRIP_OWNER_K, userId);
      tDb.upsertNewDocById(newId, props);
      if (tDb.getDocumentById(newId) != null) {
        setCurrentTripDetails("running", getTripId());

        connectGoogleClient();
        Log.d(TAG, "Successfully started by app");
        return;
      }
    }
    Log.e(TAG, "Unsuccessfully started by app");
  }

  private void resumeTrip() {
    if (!getTripId().isEmpty() && tDb.getDocumentById(getTripId()) != null) {
      if (getStatus().equals("paused")) {
        setCurrentTripDetails("running", getTripId());
      }

      connectGoogleClient();
      Log.d(TAG, "Resumed by app");
      return;
    }
    Log.e(TAG, "Unsuccessfully resumed by app");
  }

  private void pauseTrip() {
    if (!getTripId().isEmpty() && getStatus().equals("running") && tDb.getDocumentById(getTripId()) != null) {
      setCurrentTripDetails("paused", getTripId());

      stopMonitor();
      Log.d(TAG, "Paused by app");
      return;
    }
    Log.e(TAG, "Unsuccessfully paused by app");
  }

  private void stopTrip() {
    if (!getTripId().isEmpty()) {
      Map<String, Object> props = new HashMap<>();
      props.put(TRIP_STOPPED_AT, System.currentTimeMillis());
      tDb.upsertNewDocById(getTripId(), props);
      Document currTrip = tDb.getDocumentById(getTripId());

      dumpTrip(currTrip);
      //sanity check only
      if (currTrip != null && currTrip.getProperty(TRIP_STOPPED_AT) != null) {
        setCurrentTripDetails("", "");
      }
      Log.d(TAG, "Stopped by app");
      exit();
    }
    Log.e(TAG, "Unsuccessfully stopped by app");
  }

  private void dumpTrip(Document t) {
    String s = t.getProperty(TRIP_NAME_K)
        + " with preview: " + t.getProperty(TRIP_PREVIEW_K)
        + " by: " + t.getProperty(TRIP_OWNER_K)
        + " was stopped at:" + new Date((long) t.getProperty(TRIP_STOPPED_AT)).toString();
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
    while (status.equals("running")) ;
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
            public void onConnectionFailed(ConnectionResult connectionResult) {
              Log.e(TAG, "Connection Failed");
            }
          })
          .build();
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
        }
      }
    };

    //schedule the timer, to wake up every TRACKING_FREQUENCY seconds
    getTimer().schedule(locationTimerTask, 0, TRACKING_FREQUENCY);
  }

  private void addLocation(Location currentLocation) {
    Document previousLocation = TripsterDb.getInstance().getLastLocationOfTrip(currTrip.getId());
    if (previousLocation != null) {
      Location prevLoc = new Location("");
      prevLoc.setLatitude((double) previousLocation.getProperty("lat"));
      prevLoc.setLongitude((double) previousLocation.getProperty("lng"));
      if (prevLoc.distanceTo(currentLocation) > MIN_DIST) {
        saveLocation(currentLocation);
      }
    } else {
      Log.d(TAG, "Saving Trip");
      saveLocation(currentLocation);
    }
  }

  private void saveLocation(Location currentLocation) {
    String newId = UUID.randomUUID().toString();
    Map<String, Object> props = new HashMap<>();
    props.put("lat", currentLocation.getLatitude());
    props.put("lng", currentLocation.getLongitude());
    props.put("time", currentLocation.getTime());
    props.put("tripId", currTrip.getId());
    TripsterDb.getInstance().upsertNewDocById(newId, props);
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