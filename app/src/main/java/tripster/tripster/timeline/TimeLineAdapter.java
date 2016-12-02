package tripster.tripster.timeline;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.R;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

    private List<Pair<Document, List<Document>>> events;

    public TimeLineAdapter(List<Pair<Document, List<Document>>> events) {
        this.events = events;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position,getItemCount());
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_timeline, null);
        return new TimeLineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final TimeLineViewHolder holder, int position) {

        Pair<Document, List<Document>> timeLineEvent = events.get(position);

        holder.locationTextView.setText((String) timeLineEvent.first.getProperty("name"));
        holder.initView(timeLineEvent.second);
    }

    @Override
    public int getItemCount() {
        return (events!=null? events.size():0);
    }

}
