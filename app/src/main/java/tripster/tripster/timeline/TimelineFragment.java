package tripster.tripster.timeline;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.trips.tabs.Event;


public class TimelineFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<Event> events = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_timeline, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        events = this.getArguments().getParcelableArrayList("events");
        initView();

        return view;
    }

    private void initView() {
        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(events);
        mRecyclerView.setAdapter(mTimeLineAdapter);
    }

    private LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(getContext());
    }


}
