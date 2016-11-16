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
import java.util.UUID;

import static tripster.tripster.TripsterActivity.LOCATIONS_FILE_PATH;
import static tripster.tripster.TripsterActivity.SERVER_URL;

public class LocationService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TRIP_NAME = "Unnamed";
  private static final int TRACKING_FREQUENCY = 1000;
  private static final float MIN_DIST = 50;
  private static final String TAG = LocationService.class.getName();

  private GoogleApiClient googleClient;
  private Location previousLocation;
  private Timer timer;

  public LocationService() {
    super();
    googleClient = null;
    Log.i(TAG, "LocationService created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    if (intent != null) {
      Log.d(TAG, "Initial intent exists");
      if (intent.getStringExtra("flag").equals("stop")) {
        String userId = intent.getStringExtra("user_id");
        return stopRecording(userId);
      } else if (intent.getStringExtra("flag").equals("pause")) {
        return pauseRecording();
      }
    }
    return startRecording();
  }

  private int pauseRecording() {
    getTimer().cancel();
    Log.d(TAG, dumpLocationsFile());
    stopSelf();
    Log.d(TAG, "Paused by app");
    return START_NOT_STICKY;
  }

  private int startRecording() {
    checkAndInitFile();
    connectGoogleClient();
    Log.d(TAG, "Started by app");
    return START_STICKY;
  }

  private int stopRecording(String userId) {
    sendLocationsToServerAndExit(userId);
    return START_NOT_STICKY;
  }

  private void exit() {
    getTimer().cancel();
    Log.d(TAG, dumpLocationsFile());
    emptyFile();
    stopSelf();
    Log.d(TAG, "Stopped by app");
  }

  private synchronized void connectGoogleClient() {
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

    //schedule the timer, to wake up every 10 seconds
    getTimer().schedule(locationTimerTask, 0, TRACKING_FREQUENCY);
  }

  private void checkAndInitFile() {
    File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
    if (file.exists()) {
      initPreviousLocation();
    } else {
      initFile(file);
    }
  }

  private void initFile(File file) {
    try {
      Log.d(TAG, "Create file");
      FileOutputStream locationsFileStream = new FileOutputStream(file);
      OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
      try {
        String line = UUID.randomUUID().toString() + "," + TRIP_NAME;
        Log.d(TAG, "Line is:" + line);
        out.append(line);
        out.flush();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "Cannot create file" + e.toString());
    }
    Log.d(TAG, "Location file initialised.");
  }

  private void initPreviousLocation() {
    String fileStr = dumpLocationsFile();
    String[] lines = fileStr.split("\n");
    String[] lastLine = lines[lines.length - 1].split(",");
    previousLocation = new Location("");
    previousLocation.setLatitude(Double.parseDouble(lastLine[1]));
    previousLocation.setLongitude(Double.parseDouble(lastLine[2]));
    Log.d(TAG, "Previous location initialised.");
  }

  private void sendLocationsToServerAndExit(String userId) {
    final String file = dumpLocationsFile();

    Log.d(TAG, "creating request queue");
    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
    Network network = new BasicNetwork(new HurlStack());
    RequestQueue reqQ = new RequestQueue(cache, network);
    reqQ.start();

    Log.d(TAG, "sending locations to server");
    String url =  SERVER_URL + "/new_trip?user_id=" + userId;
    StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        Log.d(TAG, response);
        exit();
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Failed to send locations to server.");
        exit();
      }
    }) {
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> map = new HashMap<>();
        map.put("locations", file);
        return map;
      }
    };
    reqQ.add(strReq);
  }

  private String dumpLocationsFile() {
    StringBuilder result = new StringBuilder();
    try {
      File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line;
      while ((line = reader.readLine()) != null) {
        result.append(line);
        result.append('\n');
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }

  private Timer getTimer() {
    if (timer == null) {
      timer = new Timer();
    }
    return timer;
  }

  private void addLocation(Location location) {
    if (location != null) {
      if (previousLocation != null) {
        if (previousLocation.distanceTo(location) < MIN_DIST) {
          return;
        }
      }
      writeLocation(location);
    }
  }

  private void writeLocation(Location location) {
    previousLocation = location;
    FileOutputStream locationsFileStream;
    try {
      locationsFileStream = openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File has not been initialised");
      return;
    }
    OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
    try {
      out.append(getDetailsStr(location));
      out.flush();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.d(TAG, dumpLocationsFile());
  }

  private String getDetailsStr(Location location) {
    return "\n" + location.getTime() + ',' + location.getLatitude() + ',' + location.getLongitude();
  }

  private void emptyFile() {
    File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
    file.delete();
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