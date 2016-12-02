package tripster.tripster.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.couchbase.lite.Document;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;
import tripster.tripster.dataLayer.exceptions.AlreadyRunningTripException;
import tripster.tripster.dataLayer.exceptions.NoRunningTripsException;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks {
  private static final String TAG = LocationService.class.getName();

  private static final int TRACKING_FREQUENCY = 1000; //ms
  private static final int MIN_DIST = 50; //meters
  private static final String DEFAULT_PREVIEW = "https://cdn1.tekrevue.com/wp-content/uploads/2015/04/map-location-pin.jpg";
  private static final String DEFAULT_NAME = "Unnamed";

  private GoogleApiClient googleClient;
  private Timer timer;
  private Document currTrip;

  public LocationService() {
    super();
    currTrip = null;
    googleClient = null;
    Log.i(TAG, "LocationService created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    String userId = getSharedPreferences("UUID", MODE_PRIVATE).getString("UUID", "");
    TripsterDb.getInstance(getApplicationContext()).startSync(); // start replication and init db
    currTrip = TripsterDb.getInstance().getCurrentlyRunningTrip(userId);
    if (intent != null) {
      switch (intent.getStringExtra("flag")) {
        case "resume":
          resumeTrip(userId);
          return START_STICKY;
        case "start":
          startTrip(userId);
          return START_STICKY;
        case "pause":
          pauseTrip(userId);
          return START_NOT_STICKY;
        case "stop":
          stopTrip(userId);
          return START_NOT_STICKY;
        default:
          return START_NOT_STICKY;
      }
    } else {
      Log.d(TAG, "noIntent");
      resumeTrip(userId);
      return START_STICKY;
    }
  }

  private void resumeTrip(String userId) {
    if (currTrip != null) {
      Map<String, Object> props = new HashMap<>();
      props.put("status", "running");
      TripsterDb.getInstance().upsertNewDocById(currTrip.getId(), props);
    } else {
      throw new NoRunningTripsException("Trying to resume inexistent trip");
    }

    connectGoogleClient();
    Log.d(TAG, "Started by app");
  }

  private void startTrip(String userId) {
    if (currTrip == null) {
      String newId = UUID.randomUUID().toString();
      Map<String, Object> props = new HashMap<>();
      props.put("status", "running");
      props.put("name", DEFAULT_NAME);
      props.put("preview", DEFAULT_PREVIEW);
      props.put("ownerId", userId);
      TripsterDb.getInstance().upsertNewDocById(newId, props);
      currTrip = TripsterDb.getInstance().getHandle().getDocument(newId);
    } else {
      throw new AlreadyRunningTripException("Trying to start a trip when the previous trip is not stopped.");
    }

    connectGoogleClient();
    Log.d(TAG, "Started by app");
  }

  private void pauseTrip(String userId) {
    if (currTrip != null) {
      Map<String, Object> props = new HashMap<>();
      props.put("status", "paused");
      TripsterDb.getInstance().upsertNewDocById(currTrip.getId(), props);
    } else {
      throw new NoRunningTripsException("Trying to pause inexistent trip");
    }
    getTimer().cancel();
    timer = null;
    if (googleClient != null) {
      googleClient.disconnect();
    }
    Log.d(TAG, "Paused by app");
  }

  private void stopTrip(String userId) {
    if (currTrip != null) {
      Map<String, Object> props = new HashMap<>();
      props.put("status", "stopped");
      TripsterDb.getInstance().upsertNewDocById(currTrip.getId(), props);
      assert TripsterDb.getInstance().getCurrentlyRunningTrip(userId) == null;
    } else {
      throw new NoRunningTripsException("Trying to stop inexistent trip");
    }
    RequestQueue queue = Volley.newRequestQueue(this);
    String url ="http://146.169.46.220:8081/places?tripId=" + currTrip.getId();
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Created tripPreview at:" + response);
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Could not create preview");
      }
    });
    queue.add(stringRequest);
    dumpCurrentTrip();
    exit();
    Log.d(TAG, "Stopped by app");
  }

  private void dumpCurrentTrip() {
    String s =
        "Trip " + currTrip.getProperty("name")
        + " with preview: " + currTrip.getProperty("preview")
        + " " + currTrip.getProperty("status") + " by: " + currTrip.getProperty("ownerId");
    Log.d(TAG, "We just ended the following trip: " + s);
  }

  private String status = "initial";

  private void exit() {
    getTimer().cancel();
    timer = null;
    if (googleClient != null) {
      googleClient.disconnect();
    }
    status = "running";
    TripsterDb.getInstance().pushChanges(new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        status = response;
      }
    });
    while(status.equals("running"));
    Log.d(TAG, "closing service");
    TripsterDb.close();
    stopSelf();
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
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
        addLocation(currentLocation);
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