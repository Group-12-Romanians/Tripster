package tripster.tripster.trips.pictures;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Photo {
  private static final String TAG = Photo.class.getName();

  private String photoUri;
  private String description;

  public Photo(String photoUri, String description) {
    this.photoUri = photoUri;
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }

  public String getDescription() {
    return description;
  }

  public String getPhotoUri() {
    return photoUri;
  }

  public void displayIn(ImageView imageView) {
    Log.d(TAG, photoUri);
    Picasso.with(imageView.getContext())
        .load(photoUri)
        .resize(300, 300)
        .centerInside()
        .into(imageView);
  }
}
