package tripster.tripster.services;

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

import static android.content.Context.MODE_APPEND;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  public static final String SHARED_PREF_NAME = "TripsterPhotosIds";
  private static final String TAG = CameraEventReceiver.class.getName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "gotPic");

    Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
    if (cursor == null) {
      return;
    }
    cursor.moveToFirst();
    String image_path = cursor.getString(cursor.getColumnIndex("_data")); // _data = path to photo
    cursor.close();

    if (logPhotoInfoToFile(context, image_path)) {
      return;
    }

    Toast.makeText(context, "New Photo is Saved as : -" + image_path, Toast.LENGTH_SHORT).show();
  }

  private boolean logPhotoInfoToFile(Context context, String imagePath) {
    FileOutputStream locationsFileStream = null;
    try {
      locationsFileStream = context.openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File has not been initialised");
      return true;
    }
    OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
    try {
      out.append(",");
      out.append(generatePhotoId(context, imagePath));
      out.flush();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private String generatePhotoId(Context context, String imagePath) {
    String photoUUID = UUID.randomUUID().toString();
    SharedPreferences sharedPref
        = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    sharedPref.edit().putString(photoUUID, imagePath).apply();
    return photoUUID;
  }
}