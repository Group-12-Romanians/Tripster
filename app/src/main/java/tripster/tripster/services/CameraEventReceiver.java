package tripster.tripster.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.CURR_TRIP_ID;
import static tripster.tripster.Constants.CURR_TRIP_LL;
import static tripster.tripster.Constants.CURR_TRIP_ST;
import static tripster.tripster.Constants.PHOTO_PLACE_K;
import static tripster.tripster.Constants.PHOTO_TIME_K;
import static tripster.tripster.Constants.PHOTO_TRIP_K;
import static tripster.tripster.Constants.TRIP_PAUSED;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String TAG = CameraEventReceiver.class.getName();

  private String status = "initial";

  @Override
  public void onReceive(Context context, Intent intent) {
    AppPreferences pref = new AppPreferences(context.getApplicationContext());
    String currentTripId = pref.getString(CURR_TRIP_ID, "");
    String currentTripState = pref.getString(CURR_TRIP_ST, "");
    assertNotNull(currentTripId);
    assertNotNull(currentTripState);
    if(currentTripId.isEmpty() || currentTripState.equals(TRIP_PAUSED)) {
      return;
    }

    // Retrieve image path
    Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
    if (cursor == null) {
      return;
    }
    cursor.moveToFirst();
    String imagePath = cursor.getString(cursor.getColumnIndex("_data")); // _data = path to photo
    cursor.close();
    Log.d(TAG, "gotPic");

    // Create image doc
    String lastLocationId = pref.getString(CURR_TRIP_LL, "");
    assertNotNull(lastLocationId);
    if (lastLocationId.isEmpty()) return;
    Log.d(TAG, "got last location: " + lastLocationId);

    JSONObject props = new JSONObject();
    try {
      props.put(PHOTO_PLACE_K, lastLocationId);
      props.put(PHOTO_TRIP_K, currentTripId);
      props.put(PHOTO_TIME_K, System.currentTimeMillis());
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }

    context.getSharedPreferences("photos", Context.MODE_PRIVATE).edit().putString(imagePath, props.toString()).commit();

    // Try upload image
    new ImageUploader(context).execute();

    Toast.makeText(context.getApplicationContext(), "Tripster saved this photo.", Toast.LENGTH_LONG).show();
  }
}