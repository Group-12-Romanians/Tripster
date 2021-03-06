package tripster.tripster.UILayer.trip.timeline;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.R;

import static tripster.tripster.Constants.PLACE_DESC_K;
import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.Constants.PLACE_TRIP_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

  List<String> events;

  TimeLineAdapter(List<String> events) {
    this.events = events;
  }

  @Override
  public int getItemViewType(int position) {
    return TimelineView.getTimeLineViewType(position, getItemCount());
  }

  @Override
  public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = View.inflate(parent.getContext(), R.layout.item_timeline, null);
    return new TimeLineViewHolder(view, viewType);
  }

  @Override
  public void onBindViewHolder(final TimeLineViewHolder holder, int position) {
    String placeId = events.get(position);
    Document placeDoc = tDb.getDocumentById(placeId);
    holder.nameTextView.setText((String) placeDoc.getProperty(PLACE_NAME_K));
    String description = (String) placeDoc.getProperty(PLACE_DESC_K);
    if (description == null) {
      holder.descriptionTextView.setVisibility(View.GONE);
    } else {
      holder.descriptionTextView.setVisibility(View.VISIBLE);
      holder.descriptionTextView.setText(description);
    }
    holder.initView((String) placeDoc.getProperty(PLACE_TRIP_K), placeId);
  }

  @Override
  public int getItemCount() {
    return (events != null ? events.size() : 0);
  }

}
