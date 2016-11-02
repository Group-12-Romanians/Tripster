package tripster.tripster.pictures;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tripster.tripster.R;

public class PhotosListAdapter extends ArrayAdapter<String> {

  private final Activity activity;
  private final String[] photosDescriptions;
  private final String[] photos;

  public PhotosListAdapter(Activity activity, String[] photosDescriptions, String[] photos) {
    super(activity, R.layout.photos_list, photosDescriptions);

    this.activity = activity;
    this.photosDescriptions = photosDescriptions;
    this.photos=photos;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.photos_list, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.photo_info);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);

    txtTitle.setText(photosDescriptions[position]);
    imageView.setImageBitmap(getBitmapFromPhotoPath(photos[position]));
    return rowView;
  };

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

  private Bitmap getBitmapFromPhotoPath(String photoPath) {
    Display display = activity.getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    Log.d("width of screen", "" + size.x + ", " + size.y);

    return decodeSampledBitmapFromPath(photoPath, size.x);
  }
}