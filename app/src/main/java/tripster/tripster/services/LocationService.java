package tripster.tripster.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service
    implements GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener {

  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private static final int TRACKING_FREQUENCY = 10000;
  private static final float MIN_DIST = 200;
  private String TAG = LocationService.class.getName();

  private GoogleApiClient googleClient;
  private Timer timer;
  private TimerTask locationTimerTask;
  private Location previousLocation;

  public LocationService() {
    super();
    googleClient = null;
    Log.i(TAG, "LocationService created");
  }


  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    if (intent != null) {
      Log.d(TAG, "intent exists");
    }
    buildGoogleClientForLocationTacking();
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "ondestroy!");
    Intent broadcastIntent = new Intent("tripster.tripster.RestartSensor");
    sendBroadcast(broadcastIntent);
  }

  public void buildGoogleClientForLocationTacking() {
    //Start location tracking.
    timer = new Timer();

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

  private void initializeLocationTrackingTask() {
    locationTimerTask = new TimerTask() {
      public void run() {
        Log.d(TAG, "LocationTask is running");
        Location currentLocation
            = LocationServices.FusedLocationApi.getLastLocation(googleClient);
        addLocation(currentLocation);
        logLocations();
      }
    };
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

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public void addLocation(Location location) {
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

  private void writeLocation(Location location) {
    FileOutputStream locationsFileStream = null;
    try {
      locationsFileStream = openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
    } catch (FileNotFoundException e) {
      Log.d(TAG, "FIleNotFound");
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

  @Override
  public void onConnected(Bundle bundle) {
    Log.d("On connected", "Entered");
    initializeLocationTrackingTask();
    //schedule the timer, to wake up every 10 seconds
    timer.schedule(locationTimerTask, 0, TRACKING_FREQUENCY);
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.d(TAG, "Connection Suspended " + i);
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    Log.d(TAG, "Connection Failed");
  }
}