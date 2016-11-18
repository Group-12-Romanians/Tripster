package tripster.tripster.trips.tabs.timeline;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.trips.tabs.Event;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

    private List<Event> mEvents;

    public TimeLineAdapter(List<Event> events) {
        mEvents = events;
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
        Event timeLineEvent = mEvents.get(position);
        timeLineEvent.onPlaceFound(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                holder.locationTextView.setText(response);
            }
        });
        holder.initView(timeLineEvent);
    }

    @Override
    public int getItemCount() {
        return (mEvents!=null? mEvents.size():0);
    }

}
