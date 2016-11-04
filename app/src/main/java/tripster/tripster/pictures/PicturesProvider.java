package tripster.tripster.pictures;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PicturesProvider {
  private static final String TAG = PicturesProvider.class.getName();
  private static final String LOCATIONS_FILE_PATH = "locations.txt";

  private List<Picture> photos;

  public PicturesProvider(Activity activity) {
    photos = new ArrayList<>();
    getAllImages(activity);
  }

  public List<Picture> getPhotos() {
    return photos;
  }

  private void getAllImages(Activity activity) {
    try {
      File file = new File(activity.getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line;
      while ((line = reader.readLine()) != null) {
        Log.d(TAG, "Read line: " + line);
        String[] details = line.split(",");
        if (details.length > 3) {
          Picture photo = new Picture(Long.parseLong(details[0]), details[1], details[2], details[3]);
          photos.add(photo);
          Log.d(TAG, "Added photo: " + photo);
        }
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
