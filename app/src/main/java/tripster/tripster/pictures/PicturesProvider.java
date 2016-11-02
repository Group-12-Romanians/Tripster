package tripster.tripster.pictures;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PicturesProvider {
  private static final String TAG = PicturesProvider.class.getName();
  private List<Picture> photos;
  private Activity activity;
  private long startTime;

  public PicturesProvider(Activity activity, long startTime) {
    photos = new ArrayList<>();
    this.activity = activity;
    this.startTime = startTime;
  }

  public List<Picture> getPhotos() {
    getAllImagesPath(activity);
    return photos;
  }

  private void getAllImagesPath(Activity activity) {
    ContentResolver cr = activity.getContentResolver();

    String[] columns = new String[] {
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.TITLE,
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.MIME_TYPE,
        MediaStore.Images.ImageColumns.LONGITUDE,
        MediaStore.Images.ImageColumns.LATITUDE,
        MediaStore.Images.ImageColumns.DATE_TAKEN };

    String selection = MediaStore.Images.Media.DATE_TAKEN + " > ?";
    String[] selectionArgs = { String.valueOf(startTime) };
    Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        columns, null, null, null);

    cur.moveToFirst();
    while(!cur.isAfterLast()){
      long dateTaken     = cur.getLong(6);
      String pathToPhoto = cur.getString(2);
      String latitude    = cur.getString(5);
      String longitude   = cur.getString(4);

      if (dateTaken > startTime){
        Log.e("START TIME", startTime + "");
        Log.e("Andreea", Long.toString(dateTaken));
        Log.e("DATA", pathToPhoto);

        Picture photo = new Picture(dateTaken, latitude, longitude, pathToPhoto);
        photos.add(photo);
      }
      cur.moveToNext();
    }
  }
}
