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

public class UserTripsAdapter extends ArrayAdapter {
  private static final String TAG = UserTripsAdapter.class.getName();
  private final Context context;
  private List<Document> tripsDocuments;

  public UserTripsAdapter(Context context, int resource, int textViewResourceId, List<Document> objects) {
    super(context, resource, textViewResourceId, objects);
    this.tripsDocuments = objects;
    this.context = context;
  }

  public class ViewHolderTripPreview {
    ImageView tripPreview;
    TextView tripName;
    Document doc;
  }

  @Override
  public int getCount() {
    return tripsDocuments.size();
  }

  @Override
  public Object getItem(int position) {
    return tripsDocuments.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater view
          = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = view.inflate(R.layout.trips_grid_item, null);

      ViewHolderTripPreview holder = new ViewHolderTripPreview();

      holder.tripName = (TextView) convertView.findViewById(R.id.tripName);
      holder.tripPreview = (ImageView) convertView.findViewById(R.id.tripPrev);

      holder.doc = tripsDocuments.get(position);
      convertView.setTag(holder);
    }

    try {
      Document tripDocument = tripsDocuments.get(position);
      String tripNameString = (String) tripDocument.getProperty("name");
      String tripPreviewPhotoURI = (String) tripDocument.getProperty("preview");

      TextView tripName = ((ViewHolderTripPreview)convertView.getTag()).tripName;
      tripName.setText(tripNameString);

      ImageView tripPreview = ((ViewHolderTripPreview)convertView.getTag()).tripPreview;
      Image image = new Image(tripPreviewPhotoURI, tripNameString);
      image.displayIn(tripPreview);

    } catch (Exception e) {
      Log.d(TAG, "Cannot display trip");
      e.printStackTrace();
    }

    return convertView;
  }
}