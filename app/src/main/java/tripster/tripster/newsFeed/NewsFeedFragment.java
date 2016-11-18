package tripster.tripster.newsFeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.DefaultSingleItemCalculatorCallback;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.ListItemsVisibilityCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.SingleListViewItemActiveCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.ItemsPositionGetter;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.ListViewItemPositionGetter;
import tripster.tripster.newsFeed.video_list_demo.adapter.VideoListViewAdapter;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.DirectLinkVideoItem;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.ItemFactory;
import tripster.tripster.newsFeed.video_player_manager.manager.PlayerItemChangeListener;
import tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;
import tripster.tripster.newsFeed.video_player_manager.utils.Logger;
import tripster.tripster.trips.TripPreview;

/**
 * This fragment shows of how to use {@link tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager} with a ListView.
 */
public class NewsFeedFragment extends Fragment {

  private static final boolean SHOW_LOGS = false;
  private static final String TAG = NewsFeedFragment.class.getSimpleName();

  private ArrayList<DirectLinkVideoItem> mList;

  private ListItemsVisibilityCalculator mListItemVisibilityCalculator;

  private ItemsPositionGetter mItemsPositionGetter;

  private VideoPlayerManager<MetaData> mVideoPlayerManager;

  private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

  private ListView mListView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mList = new ArrayList<>();
    mListItemVisibilityCalculator
        = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList);
    mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
      @Override
      public void onPlayerItemChanged(MetaData metaData) {
        if (SHOW_LOGS) Logger.v(TAG, "onPlayerItemChanged " + metaData);
      }
    });
    String trips = this.getArguments().getString("trips");
    List<TripPreview> tripPreviews = processNewsFeedServerResponse(trips);
    for (TripPreview t : tripPreviews) {
      mList.add(ItemFactory.createItemFomDirectLink(
          t.getName(),
          t.getPreviewVideo(),
          t.getPreviewURI(),
          getActivity(),
          mVideoPlayerManager,
          t.getOwner(),
          t.getOwnerAvatar(),
          t.getId()));
    }

    View rootView = inflater.inflate(R.layout.fragment_video_list_view, container, false);

    mListView = (ListView) rootView.findViewById(R.id.list_view);
    VideoListViewAdapter videoListViewAdapter = new VideoListViewAdapter(mVideoPlayerManager, getActivity(), mList);
    mListView.setAdapter(videoListViewAdapter);

    Log.d(TAG, "Here");
    mItemsPositionGetter = new ListViewItemPositionGetter(mListView);
    
    mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
        if (scrollState == SCROLL_STATE_IDLE && !mList.isEmpty()) {
          mListItemVisibilityCalculator.onScrollStateIdle(mItemsPositionGetter, view.getFirstVisiblePosition(), view.getLastVisiblePosition());
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!mList.isEmpty()) {
          mListItemVisibilityCalculator.onScroll(mItemsPositionGetter, firstVisibleItem, visibleItemCount, mScrollState);
        }
      }
    });
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mList.isEmpty()) {
      // need to call this method from list view handler in order to have list filled previously

      mListView.post(new Runnable() {
        @Override
        public void run() {
          mListItemVisibilityCalculator.onScrollStateIdle(
              mItemsPositionGetter,
              mListView.getFirstVisiblePosition(),
              mListView.getLastVisiblePosition());
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

  private List<TripPreview> processNewsFeedServerResponse(String response) {
    List<TripPreview> tripPreviews = new ArrayList<>();
    try {
      JSONArray tripsJSON = new JSONArray(response);
      for (int i = 0; i < tripsJSON.length(); i++) {
        JSONObject tripJSON = tripsJSON.getJSONObject(i);
        JSONObject userJSON = tripJSON.getJSONArray("user").getJSONObject(0);
        TripPreview tripPreview = new TripPreview(tripJSON.getString("trip_id"),
            tripJSON.getString("name"),
            userJSON.getString("name"),
            userJSON.getString("avatar"),
            tripJSON.getString("preview_img"),
            tripJSON.getString("preview_video"));
        tripPreviews.add(tripPreview);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return tripPreviews;
  }
}
