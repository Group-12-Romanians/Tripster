package tripster.tripster.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
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
  private final Bitmap[] photos;

  public PhotosListAdapter(Activity activity, String[] photosDescriptions, Bitmap[] photos) {
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
    imageView.setImageBitmap(photos[position]);
    return rowView;
  }

}