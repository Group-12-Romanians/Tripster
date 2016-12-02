package tripster.tripster;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image {
  private static final String TAG = Image.class.getName();

  private String photoUri;
  private String description;

  public Image(String photoUri, String description) {
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
      Log.e(TAG, "no Image URI");
    }
  }
}
