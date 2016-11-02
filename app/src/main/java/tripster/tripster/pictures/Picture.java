package tripster.tripster.pictures;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class Picture {

  private String latitude;
  private String longitude;
  private String pathToPhoto;

  public Bitmap getPhoto() {
    return photo;
  }

  private Bitmap photo;
  private long dateTaken;

  public Picture(long dateTaken, String latitude, String longitude, String pathToPhoto) {
    this.dateTaken = dateTaken;
    this.latitude = latitude;
    this.longitude = longitude;
    this.pathToPhoto = pathToPhoto;
    photo = getBitmapFromPhotoPath(pathToPhoto);
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

  private Bitmap getBitmapFromPhotoPath(String photoPath) {
    File image = new File(photoPath);
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
    return bitmap;
  }
}
