package tripster.tripster;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image {
  private static final String TAG = Image.class.getName();

  private String photoUri;

  public Image(String photoUri) {
    this.photoUri = photoUri;
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
