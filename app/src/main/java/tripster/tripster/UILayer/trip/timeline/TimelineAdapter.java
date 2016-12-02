package tripster.tripster.UILayer.trip.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import tripster.tripster.Image;
import tripster.tripster.R;

public class TimelineAdapter extends ArrayAdapter {

  private static final String TAG = TimelineAdapter.class.getName();
  private List<Pair<Document, List<Document>>> events;
  private Context context;

  public TimelineAdapter(Context context, int resource, int textViewResourceId, List<Pair<Document, List<Document>>> events) {
    super(context, resource, textViewResourceId, events);
    this.events = events;
    this.context = context;
  }

  public class ViewHolderTimelinePreview {
    // Location details
    TextView location;
    TextView time;
    TextView locationDescription;
    Document locationDoc;
    // Trip details
    ImageView locationPreview;
  }

  @Override
  public int getCount() {
    return events.size();
  }

  @Override
  public Object getItem(int position) {
    return events.get(position);
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
      convertView = view.inflate(R.layout.event, null);
      ViewHolderTimelinePreview holder = new ViewHolderTimelinePreview();
      // Set location details
      holder.location = (TextView) convertView.findViewById(R.id.locationName);
      holder.time = (TextView) convertView.findViewById(R.id.time);
      holder.locationDescription = (TextView) convertView.findViewById(R.id.description);
      holder.locationDoc = events.get(position).first;
      // Set preview photo
      holder.locationPreview = (ImageView) convertView.findViewById(R.id.locationPreview);
      convertView.setTag(holder);
    }

    try {
      Pair<Document, List<Document>> eventDocs = events.get(position);
      final Document locationDoc = eventDocs.first;
      final List<Document> photosDocs = eventDocs.second;

      // Set location name
      // TODO: Call google APi to get location.
      String locationName = "Location";
      TextView locationNameView = ((ViewHolderTimelinePreview) convertView.getTag()).location;
      locationNameView.setText(locationName);
      // Set location description
      /*String locationDescription = (String) locationDoc.getProperty("description");
      TextView locationDescriptionView = ((ViewHolderTimelinePreview) convertView.getTag()).locationDescription;
      locationDescriptionView.setText(locationDescription);*/
      // Set time
      SimpleDateFormat dateFormat = new SimpleDateFormat();
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      String time = dateFormat.format(new Date((long) locationDoc.getProperty("time")));
      TextView timeView = ((ViewHolderTimelinePreview) convertView.getTag()).time;
      timeView.setText(time);
      // Set location preview.
      Document firstPhoto = photosDocs.get(0);
      String photoUrl = (String) firstPhoto.getProperty("path");
      Image locationPreview = new Image(photoUrl, "This is a description");
      ImageView locationPreviewView = ((ViewHolderTimelinePreview) convertView.getTag()).locationPreview;
      locationPreview.displayIn(locationPreviewView);
      // Set slider
      locationPreviewView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
      });
    } catch (Exception e) {
      Log.d(TAG, "Cannot display location");
      e.printStackTrace();
    }
    return convertView;
  }

}
