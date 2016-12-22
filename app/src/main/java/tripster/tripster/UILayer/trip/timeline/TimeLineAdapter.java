package tripster.tripster.UILayer.trip.timeline;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.R;

import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

  private List<Pair<String, List<String>>> events;

  TimeLineAdapter(List<Pair<String, List<String>>> events) {
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
    Pair<String, List<String>> timeLineEvent = events.get(position);

    String placeId = timeLineEvent.first;
    Document placeDoc = tDb.getDocumentById(placeId);
    holder.locationTextView.setText((String) placeDoc.getProperty(PLACE_NAME_K));
    holder.initView(timeLineEvent.second);
  }

  @Override
  public int getItemCount() {
    return (events != null ? events.size() : 0);
  }

}
