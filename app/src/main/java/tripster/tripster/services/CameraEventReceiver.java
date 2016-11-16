package tripster.tripster.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;
import static tripster.tripster.TripsterActivity.LOCATIONS_FILE_PATH;
import static tripster.tripster.TripsterActivity.SHARED_PREF_PHOTOS;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String TAG = CameraEventReceiver.class.getName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "gotPic");

    Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
    if (cursor == null) {
      return;
    }
    cursor.moveToFirst();
    String imagePath = cursor.getString(cursor.getColumnIndex("_data")); // _data = path to photo
    cursor.close();

    String photoId = generateAndSavePhotoId(context, imagePath);

    if (!logPhotoInfoToFile(context, photoId)) {
      return;
    }

    new ImageUploader().execute(photoId, imagePath);

    Toast.makeText(context, "Tripster saved this photo.", Toast.LENGTH_SHORT).show();
  }

  private boolean logPhotoInfoToFile(Context context, String photoId) {
    if (isServiceRunning(context)) {
      FileOutputStream locationsFileStream;
      try {
        locationsFileStream = context.openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
      } catch (FileNotFoundException e) {
        Log.e(TAG, "File has not been initialised");
        return false;
      }
      OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
      try {
        out.append(",");
        out.append(photoId);
        out.flush();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }
    return false;
  }

  private String generateAndSavePhotoId(Context context, String imagePath) {
    String photoUUID = UUID.randomUUID().toString();
    SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREF_PHOTOS, MODE_PRIVATE);
    sharedPref.edit().putString(photoUUID, imagePath).apply();
    return photoUUID;
  }

  private boolean isServiceRunning(Context context) {
    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (LocationService.class.getName().equals(service.service.getClassName())) {
        Log.i("isMyServiceRunning?", true + "");
        return true;
      }
    }
    Log.i("isMyServiceRunning?", false + "");
    return false;
  }
}