package tripster.tripster;

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
    if (photoUri != null) {
      Picasso.with(imageView.getContext())
          .load(photoUri)
          .fit()
          .centerInside()
          .into(imageView);
    } else {
      Log.e(TAG, "no Photo URI");
    }
  }
}