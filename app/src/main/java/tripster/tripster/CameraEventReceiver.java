package tripster.tripster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_APPEND;

public class CameraEventReceiver extends BroadcastReceiver {
  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private static final String TAG = CameraEventReceiver.class.getName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("HAHAHHAHAHHA", "gotPic");

    Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
    cursor.moveToFirst();
    String image_path = cursor.getString(cursor.getColumnIndex("_data")); // _data = path to photo
    long dateTaken = cursor.getLong(cursor.getColumnIndex("datetaken"));

    try {
      File file = new File(context.getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line, lastLine = "";
      while ((line = reader.readLine()) != null) {
        lastLine = line;
      }
      if (lastLine.contains("paused")) {
        Log.d(TAG, "Service is paused so no need to write picture");
      } else {
        reader.close();
        FileOutputStream locationsFileStream = context.openFileOutput(LOCATIONS_FILE_PATH, MODE_APPEND);
        OutputStreamWriter out = new OutputStreamWriter(locationsFileStream);
        String[] lastLineDetails = lastLine.split(",");
        String details = dateTaken + "," + lastLineDetails[1] + "," + lastLineDetails[2] + "," + image_path + "\n";
        out.append(details);
        out.flush();
        out.close();
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from so service must be paused");
      return;
    } catch (IOException e) {
      e.printStackTrace();
    }

    Toast.makeText(context, "New Photo is Saved as : -" + image_path, Toast.LENGTH_SHORT).show();
  }
}