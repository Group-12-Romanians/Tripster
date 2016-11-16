package tripster.tripster.trips.pictures;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

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
    if (localFile(photoUri)) {
      Picasso.with(imageView.getContext())
          .load(new File(photoUri))
          .fit()
          .centerInside()
          .into(imageView);
    } else {
      Picasso.with(imageView.getContext())
          .load(photoUri)
          .fit()
          .centerInside()
          .into(imageView);
    }
  }

  private boolean localFile(String photoUri) {
    return photoUri.charAt(0) == '/';
  }
}
