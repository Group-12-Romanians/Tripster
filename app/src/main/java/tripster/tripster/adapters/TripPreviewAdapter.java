package tripster.tripster.adapters;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tripster.tripster.R;

public class TripPreviewAdapter extends BaseAdapter {
  private Activity activity;
  private String[] tripNames = {"Ongoing Trip", "Trip", "Trip", "Trip"};

  public TripPreviewAdapter(Activity activity) {
    this.activity = activity;
  }

  public int getCount() {
    return thumbIds.length;
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
      rowView = inflater.inflate(R.layout.photos_list, null,true);
      TextView txtTitle = (TextView) rowView.findViewById(R.id.photo_info);
      ImageView imageView = (ImageView) rowView.findViewById(R.id.photo);

      txtTitle.setText(tripNames[position]);
      txtTitle.setGravity(Gravity.CENTER);
      imageView.setImageResource(thumbIds[position]);
    } else {
      rowView = convertView;
    }
    return rowView;
  }

  private Integer[] thumbIds = {
      R.drawable.bubble_mask, R.drawable.bubble_mask, R.drawable.bubble_mask, R.drawable.bubble_mask};
}