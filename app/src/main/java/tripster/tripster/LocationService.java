package tripster.tripster;

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

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service
    implements GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener {

  private static final int TRACKING_FREQUENCY = 10000;
  private String TAG = LocationService.class.getName();

  private GoogleApiClient googleClient;
  private HashSet<Location> locations;
  private Timer timer;
  private TimerTask locationTimerTask;

  public LocationService() {
    super();
    googleClient = null;
    locations = new HashSet<>();
    Log.i(TAG, "LocationService created");
  }


  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
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
        logDetectedLocations();
      }
    };
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }


  public void addLocation(Location location) {
    if (location != null) {
      locations.add(location);
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

  private void logDetectedLocations() {
    String locationsDetails = "";
    for (Location location : locations) {
      locationsDetails += "(" + location.getLatitude() + ", " + location.getLongitude() + "),";
    }
    Log.d(LocationService.class.getName(), locationsDetails);
  }
}