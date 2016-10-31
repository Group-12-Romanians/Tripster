package tripster.tripster.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service
    implements GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener {

  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private static final String SERVER_URL = "https://tripster-serverside.herokuapp.com/sync_locations";
  private static final int TRACKING_FREQUENCY = 1000;
  private static final int SYNC_FREQUENCY = 5000;
  private static final float MIN_DIST = 200;
  private String TAG = LocationService.class.getName();

  private GoogleApiClient googleClient;
  private TimerTask locationTimerTask;
  private Location previousLocation;
  private RequestQueue reqQ;
  private Timer timer;

  public LocationService() {
    super();
    googleClient = null;
    reqQ = null;
    Log.i(TAG, "LocationService created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    if (intent != null) {
      Log.d(TAG, "Initial intent exists");
      if (intent.getStringExtra("flag").equals("start")) {
        startRecording();
      } else if (intent.getStringExtra("flag").equals("stop")) {
        stopRecording();
      } else {
        Log.d(TAG, "Flag not recognised");
      }
    } else {
      startRecording();
    }
    return START_STICKY;
  }

  private void startRecording() {
    startSyncLocations();
    startLocationTracking();
    Log.d(TAG, "Started by app");
  }

  private void startSyncLocations() {
    Log.d(TAG, "creating request queue");
    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
    Network network = new BasicNetwork(new HurlStack());
    reqQ = new RequestQueue(cache, network);
    reqQ.start();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        Log.d(TAG, "syncing");
        StringRequest strReq = new StringRequest(Request.Method.POST, SERVER_URL, new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d("onResponse", "mamamama");
          }
        }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("onErrorResponse", "tatatatat");
          }
        }) {
          @Override
          protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> map = new HashMap<>();
            Log.d(TAG, "Dargps");
            map.put("locations", "Dragos");
            return map;
          }
        };
        reqQ.add(strReq);
      }
    };
    getTimer().schedule(task, SYNC_FREQUENCY, SYNC_FREQUENCY);
  }

  private void stopRecording() {
    timer.cancel();
    logLocations();
    emptyFile();
    stopSelf();
    Log.d(TAG, "Stopped by app");
  }

  @Override
  public void onConnected(Bundle bundle) {
    Log.d("On connected", "Entered");
    initializeLocationTrackingTask();
    //schedule the timer, to wake up every 10 seconds
    getTimer().schedule(locationTimerTask, 0, TRACKING_FREQUENCY);
  }

  private Timer getTimer() {
    if (timer == null) {
      timer = new Timer();
    }
    return timer;
  }

  private void initializeLocationTrackingTask() {
    locationTimerTask = new TimerTask() {
      public void run() {
        Log.d(TAG, "LocationTask is running");
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
        addLocation(currentLocation);
        logLocations();
      }
    };
  }

  private void addLocation(Location location) {
    if (location != null) {
      if (previousLocation != null) {
        if (previousLocation.distanceTo(location) < MIN_DIST) {
          return;
        }
      }
      previousLocation = location;
      writeLocation(location);
    }
  }

  private synchronized void startLocationTracking() {
    if (googleClient == null) {
      googleClient = new GoogleApiClient.Builder(this)
              .addApi(LocationServices.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();
    }
    if (!googleClient.isConnected()) {
      googleClient.connect();
    }
  }

  private void writeLocation(Location location) {
    FileOutputStream locationsFileStream = null;
    try {
      locationsFileStream = openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
    } catch (FileNotFoundException e) {
      File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
      try {
        locationsFileStream = new FileOutputStream(file);
      } catch (FileNotFoundException e1) {
        Log.d(TAG, "FileNotFound");
      }
    }
    OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
    try {
      out.append(location.toString() + "\n");
      out.flush();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void emptyFile() {
    File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
    file.delete();
  }

  private void logLocations() {
    try {
      File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line;
      while ((line = reader.readLine()) != null) {
        Log.d(TAG, "Read line: " + line);
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.d(TAG, "Connection Suspended " + i);
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    Log.d(TAG, "Connection Failed");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}