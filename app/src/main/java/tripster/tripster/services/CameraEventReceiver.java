package tripster.tripster.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.couchbase.lite.Document;

import net.grandcentrix.tray.AppPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.CURR_TRIP;
import static tripster.tripster.Constants.PHOTO_PATH_K;
import static tripster.tripster.Constants.PHOTO_PLACE_K;
import static tripster.tripster.Constants.PHOTO_TIME_K;
import static tripster.tripster.Constants.PHOTO_TRIP_K;
import static tripster.tripster.Constants.SERVER_URL;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String TAG = CameraEventReceiver.class.getName();

  private String status = "stopped";

  @Override
  public void onReceive(Context context, Intent intent) {
    String userId = context.getSharedPreferences("UUID", MODE_PRIVATE).getString("UUID", "");
    TripsterDb tDb = new TripsterDb(context.getApplicationContext());
    AppPreferences pref = new AppPreferences(context.getApplicationContext());
    String currentTripId = pref.getString(CURR_TRIP, "");
    assertNotNull(currentTripId);

    if(currentTripId.isEmpty()) {
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

    // Upload image
    new ImageUploader().execute(photoId, imagePath);

    // Insert image in DB
    Document lastLocationInTripDoc = tDb.getLastLocationOfTrip(currentTripId);
    if (lastLocationInTripDoc != null) {
      String locationId = lastLocationInTripDoc.getId();
      Map<String, Object> props = new HashMap<>();
      props.put(PHOTO_PLACE_K, locationId);
      props.put(PHOTO_PATH_K, SERVER_URL + "/" + photoId + ".jpg");
      props.put(PHOTO_TRIP_K, currentTripId);
      props.put(PHOTO_TIME_K, System.currentTimeMillis());
      tDb.upsertNewDocById(photoId, props);
    }

    Toast.makeText(context, "Tripster saved this photo.", Toast.LENGTH_SHORT).show();

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