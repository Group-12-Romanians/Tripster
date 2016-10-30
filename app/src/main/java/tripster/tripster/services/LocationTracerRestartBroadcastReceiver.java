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

    context.startService(new Intent(context, LocationService.class));
  }
}
