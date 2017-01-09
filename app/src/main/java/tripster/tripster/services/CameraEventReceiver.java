package tripster.tripster.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;

import net.grandcentrix.tray.AppPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;

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
    TripsterDb tDb = TripsterDb.getInstance(context.getApplicationContext());
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

    // Generate Image Id
    String photoId = UUID.randomUUID().toString();

    context.getSharedPreferences("photos", Context.MODE_PRIVATE).edit().putString(imagePath, photoId).commit();

    // Try upload image
    new ImageUploader(context).execute();

    // Insert image in DB
    String lastLocationId = pref.getString(CURR_TRIP_LL, "");
    assertNotNull(lastLocationId);

    if (!lastLocationId.isEmpty()) {
      Log.d(TAG, "got last location: " + lastLocationId);
      Map<String, Object> props = new HashMap<>();
      props.put(PHOTO_PLACE_K, lastLocationId);
      props.put(PHOTO_TRIP_K, currentTripId);
      props.put(PHOTO_TIME_K, System.currentTimeMillis());
      tDb.upsertNewDocById(photoId, props);
    }

    Toast.makeText(context.getApplicationContext(), "Tripster saved this photo.", Toast.LENGTH_LONG).show();

    status = "running";
    tDb.pushChanges(new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        status = response;
      }
    });
    while(status.equals("running"));
    Log.d(TAG, "Closing receiver");
  }
}