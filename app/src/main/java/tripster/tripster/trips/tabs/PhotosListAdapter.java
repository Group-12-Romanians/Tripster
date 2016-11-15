package tripster.tripster.trips.tabs;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.trips.pictures.Photo;

public class PhotosListAdapter extends ArrayAdapter<Photo> {

  private final Activity activity;
  private final List<Photo> photos;

  public PhotosListAdapter(Activity activity, List<Photo> photos) {
    super(activity, R.layout.photos_list, photos);

    this.activity = activity;
    this.photos=photos;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.photos_list, null,true);

    TextView txtTitle = (TextView) rowView.findViewById(R.id.photo_info);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);

    txtTitle.setText(photos.get(position).getDescription());

    photos.get(position).displayIn(imageView);
    return rowView;
  }

}