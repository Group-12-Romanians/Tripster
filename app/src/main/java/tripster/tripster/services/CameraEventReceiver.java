package tripster.tripster.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;

import static android.content.Context.MODE_PRIVATE;
import static tripster.tripster.UILayer.TripsterActivity.SERVER_URL;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String TAG = CameraEventReceiver.class.getName();

  private String status = "initial";

  @Override
  public void onReceive(Context context, Intent intent) {
    String userId = context.getSharedPreferences("UUID", MODE_PRIVATE).getString("UUID", "");
    TripsterDb.getInstance(context.getApplicationContext());
    Document currentTripDoc = TripsterDb.getInstance().getCurrentlyRunningTrip(userId);
    if (currentTripDoc != null) {
      Log.d(TAG, "gotPic");

      // Retrieve image path
      Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
      if (cursor == null) {
        return;
      }
      cursor.moveToFirst();
      String imagePath = cursor.getString(cursor.getColumnIndex("_data")); // _data = path to photo
      cursor.close();

      // Generate Photo Id
      String photoId = UUID.randomUUID().toString();
      status = "running";

      // Upload image
      new ImageUploader().execute(photoId, imagePath);

      // Insert image in DB
      Document lastLocationInTripDoc = TripsterDb.getInstance().getLastLocationOfTrip(currentTripDoc.getId());
      if (lastLocationInTripDoc != null) {
        String locationId = lastLocationInTripDoc.getId();
        Map<String, Object> props = new HashMap<>();
        props.put("placeId", locationId);
        props.put("path", SERVER_URL + "/" + photoId + ".jpg");
        props.put("tripId", currentTripDoc.getId());
        props.put("time", System.currentTimeMillis());
        TripsterDb.getInstance().upsertNewDocById(photoId, props);
      }
      TripsterDb.getInstance().pushChanges(new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          status = response;
        }
      });
      Toast.makeText(context, "Tripster saved this photo.", Toast.LENGTH_SHORT).show();
    }
    while(status.equals("running"));
    Log.d(TAG, "Closing receiver");
    TripsterDb.close();
  }
}