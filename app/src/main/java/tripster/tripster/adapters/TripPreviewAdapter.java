package tripster.tripster.adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.Trip;

public class TripPreviewAdapter extends BaseAdapter {
  private Activity activity;
  private List<Trip> trips;

  public TripPreviewAdapter(Activity activity, List<Trip> trips) {
    this.activity = activity;
    this.trips = trips;
  }

  public int getCount() {
    return trips.size();
  }

  public Object getItem(int position) {
    return null;
  }

  public long getItemId(int position) {
    return 0;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    View rowView;
    if (convertView == null) {  // if it's not recycled, initialize some attributes
      LayoutInflater inflater = activity.getLayoutInflater();
      rowView = inflater.inflate(R.layout.photos_list, null, true);

      Trip trip = trips.get(position);

      TextView txtTitle = (TextView) rowView.findViewById(R.id.photo_info);
      txtTitle.setText(trip.getName());
      txtTitle.setGravity(Gravity.CENTER);

      ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);
      imageView.setImageURI(Uri.parse(trip.getPreviewURI()));

      rowView.setTag(trip);
    } else {
      rowView = convertView;
    }
    return rowView;
  }
}