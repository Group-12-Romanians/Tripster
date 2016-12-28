package tripster.tripster.UILayer.users;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.DEFAULT_NAME;
import static tripster.tripster.Constants.DEFAULT_PREVIEW;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.R.id.tripName;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class UserTripsAdapter extends ArrayAdapter<String> {
  private static final String TAG = UserTripsAdapter.class.getName();

  private List<String> trips;
  private LayoutInflater inflater;
  private TransactionManager tM;

  UserTripsAdapter(Context context, int resource, int textViewResourceId, List<String> trips) {
    super(context, resource, textViewResourceId, trips);
    this.trips = trips;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    tM = new TransactionManager(getContext());
  }

  private class ViewHolder {
    ImageView tripPreview;
    TextView tripName;
  }

  @Override
  public int getCount() {
    return trips.size();
  }

  @Override
  public String getItem(int position) {
    return trips.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.trips_grid_item, null);
      ViewHolder holder = new ViewHolder();
      holder.tripName = (TextView) convertView.findViewById(tripName);
      holder.tripPreview = (ImageView) convertView.findViewById(R.id.tripPrev);
      convertView.setTag(holder);
    }
    try {
      final String tripId = trips.get(position);
      Document tripDocument = tDb.getDocumentById(tripId);

      convertView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          tM.accessTrip(tripId);
        }
      });

      TextView nameView = ((ViewHolder) convertView.getTag()).tripName;
      String name = (String) tripDocument.getProperty(TRIP_NAME_K);
      if (name == null) {
        name = DEFAULT_NAME;
      }
      nameView.setText(name);

      ImageView previewView = ((ViewHolder) convertView.getTag()).tripPreview;
      String preview = (String) tripDocument.getProperty(TRIP_PREVIEW_K);
      if (preview == null) {
        preview = DEFAULT_PREVIEW;
      }
      new Image(preview).displayIn(previewView);
    } catch (Exception e) {
      Log.e(TAG, "Cannot display trip");
      e.printStackTrace();
    }
    return convertView;
  }
}