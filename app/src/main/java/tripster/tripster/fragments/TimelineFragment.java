package tripster.tripster.fragments;


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
import tripster.tripster.TimeLineAdapter;
import tripster.tripster.TimeLineModel;


public class TimelineFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<TimeLineModel> mDataList = new ArrayList<>();

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//
//        super.onActivityCreated(savedInstanceState);
//
//        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
//        mRecyclerView.setLayoutManager(getLinearLayoutManager());
//        mRecyclerView.setHasFixedSize(true);
//
//        initView();
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_timeline, container, false);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        initView();

        return view;
    }

    private void initView() {

        for(int i = 0;i <20;i++) {
            TimeLineModel model = new TimeLineModel();
            model.setName("Random"+i);
            model.setAge(i);
            mDataList.add(model);
        }

        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(mDataList);
        mRecyclerView.setAdapter(mTimeLineAdapter);

    }

    private LinearLayoutManager getLinearLayoutManager() {

        return new LinearLayoutManager(getContext());

    }


}
