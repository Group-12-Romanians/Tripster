package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;

import tripster.tripster.R;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.DefaultSingleItemCalculatorCallback;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.ListItemsVisibilityCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.SingleListViewItemActiveCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.ItemsPositionGetter;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.RecyclerViewItemPositionGetter;
import tripster.tripster.newsFeed.video_list_demo.adapter.VideoRecyclerViewAdapter;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.BaseVideoItem;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.ItemFactory;
import tripster.tripster.newsFeed.video_player_manager.Config;

public class NewsFeedFragment extends Fragment {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = NewsFeedFragment.class.getSimpleName();

    private final ArrayList<BaseVideoItem> mList = new ArrayList<>();

    /**
     * Only the one (most visible) view should be active (and playing).
     * To calculate visibility of views we use {@link SingleListViewItemActiveCalculator}
     */
    private final ListItemsVisibilityCalculator mVideoVisibilityCalculator =
            new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList);

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    /**
     * ItemsPositionGetter is used by {@link ListItemsVisibilityCalculator} for getting information about
     * items position in the RecyclerView and LayoutManager
     */
    private ItemsPositionGetter mItemsPositionGetter;

    /**
     * Here we use {@link tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager}, which means that only one video playback is possible.
     */
    private final tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager<tripster.tripster.newsFeed.video_player_manager.meta.MetaData> mVideoPlayerManager = new tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager(new tripster.tripster.newsFeed.video_player_manager.manager.PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(tripster.tripster.newsFeed.video_player_manager.meta.MetaData metaData) {

        }
    });

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mList.add(ItemFactory.createItemFomDirectLink("Trip Italy 1",
                "https://www.youtube.com/watch?v=GzKFEx-wsJo&ab_channel=Izabela%C5%9Awirkowska",
                R.mipmap.video_sample_1_pic, getActivity(), mVideoPlayerManager));

//        mList.add(ItemFactory.createItemFomDirectLink("Trip Italy 2",
//                "https://www.youtube.com/watch?v=ezSD8F5zQqk&list=PL-ZNkGMYczu566sSLNooxhcaEsIuX_ntk&index=14&ab_channel=mithosDC",
//                R.mipmap.video_sample_2_pic, getActivity(), mVideoPlayerManager));


        View rootView = inflater.inflate(R.layout.fragment_video_recycler_view, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        VideoRecyclerViewAdapter videoRecyclerViewAdapter =
                            new VideoRecyclerViewAdapter(mVideoPlayerManager, getActivity(), mList);

        mRecyclerView.setAdapter(videoRecyclerViewAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                mScrollState = scrollState;
                if(scrollState == RecyclerView.SCROLL_STATE_IDLE && !mList.isEmpty()){

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!mList.isEmpty()){
                    mVideoVisibilityCalculator.onScroll(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition() - mLayoutManager.findFirstVisibleItemPosition() + 1,
                            mScrollState);
                }
            }
        });
        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLayoutManager, mRecyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mList.isEmpty()){
            // need to call this method from list view handler in order to have filled list

            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());

                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // we have to stop any playback in onStop
        mVideoPlayerManager.resetMediaPlayer();
    }
}
