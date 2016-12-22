package tripster.tripster.UILayer.trip.editable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.List;

import tripster.tripster.R;

public class PlaceListAdapter extends ArrayAdapter {
  private List<Place> places;
  private Context context;

  public PlaceListAdapter(Context context, int resource, int textViewResourceId, List<Place> places) {
    super(context, resource, textViewResourceId, places);
    this.places = places;
    this.context = context;
  }

  public class ViewHolderTimelinePreview {
    // Location details
    EditText placeName;
    EditText placeDescription;
  }

  @Override
  public int getCount() {
    return places.size();
  }

  @Override
  public Object getItem(int position) {
    return places.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater view
          = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = view.inflate(R.layout.editable_place, null);
      ViewHolderTimelinePreview holder = new ViewHolderTimelinePreview();
      // Set location details
      holder.placeName = (EditText) convertView.findViewById(R.id.locationName);
      holder.placeDescription = (EditText) convertView.findViewById(R.id.description);
      convertView.setTag(holder);
    }

    EditText placeNameView = ((ViewHolderTimelinePreview) convertView.getTag()).placeName;
    placeNameView.setText(places.get(position).getName());

    EditText placeDescriptionView = ((ViewHolderTimelinePreview) convertView.getTag()).placeDescription;
    placeDescriptionView.setText(places.get(position).getDescription());
    return convertView;
  }
}
