package tripster.tripster.pictures;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Picture {

  private String latitude;
  private String longitude;
  private String pathToPhoto;

  private long dateTaken;

  public Picture(long dateTaken, String latitude, String longitude, String pathToPhoto) {
    this.dateTaken = dateTaken;
    this.latitude = latitude;
    this.longitude = longitude;
    this.pathToPhoto = pathToPhoto;
  }
  
  public static Bitmap decodeSampledBitmapFromPath(String photoPath, int reqWidth) {
    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(photoPath, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(photoPath, options);
  }

  public static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getPathToPhoto() {
    return pathToPhoto;
  }

  public String getLongitude() {
    return longitude;
  }

  public long getDateTaken() {
    return dateTaken;
  }

  @Override
  public String toString() {
    return pathToPhoto + ", latitude: " + latitude + ", logitude: " + longitude + "date: " + dateTaken;
  }


  public Bitmap getBitmap(int maxWidth) {
    return decodeSampledBitmapFromPath(pathToPhoto, maxWidth);
  }
}
