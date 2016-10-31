package tripster.tripster.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationTracerRestartBroadcastReceiver extends BroadcastReceiver {
  private String TAG
      = LocationTracerRestartBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Service Stopped");
    Intent serviceIntent = new Intent(context, LocationService.class);
    serviceIntent.putParcelableArrayListExtra("locations", intent.getParcelableArrayListExtra("locations"));
    serviceIntent.putExtra("flag", intent.getStringExtra("flag"));
    context.startService(serviceIntent);
  }
}
