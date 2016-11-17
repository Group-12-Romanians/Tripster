package tripster.tripster.newsFeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;
import tripster.tripster.User;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.DefaultSingleItemCalculatorCallback;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.ListItemsVisibilityCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.calculator.SingleListViewItemActiveCalculator;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.ItemsPositionGetter;
import tripster.tripster.newsFeed.list_visibility_utils.scroll_utils.ListViewItemPositionGetter;
import tripster.tripster.newsFeed.video_list_demo.adapter.VideoListViewAdapter;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.DirectLinkVideoItem;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.ItemFactory;
import tripster.tripster.newsFeed.video_player_manager.Config;
import tripster.tripster.newsFeed.video_player_manager.manager.PlayerItemChangeListener;
import tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;
import tripster.tripster.newsFeed.video_player_manager.utils.Logger;

/**
 * This fragment shows of how to use {@link tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager} with a ListView.
 */
public class NewsFeedFragment extends Fragment {

  private static final String NEWS_FEED_URL = TripsterActivity.SERVER_URL + "/trips";
  private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
  private static final String TAG = NewsFeedFragment.class.getSimpleName();

  private ArrayList<DirectLinkVideoItem> mList;

  private List<User> users;
  /**
   * Only the one (most visible) view should be active (and playing).
   * To calculate visibility of views we use {@link SingleListViewItemActiveCalculator}
   */
  private ListItemsVisibilityCalculator mListItemVisibilityCalculator;

  /**
   * ItemsPositionGetter is used by {@link ListItemsVisibilityCalculator} for getting information about
   * items position in the ListView
   */
  private ItemsPositionGetter mItemsPositionGetter;

  /**
   * Here we use {@link tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager}, which means that only one video playback is possible.
   */
  private VideoPlayerManager<MetaData> mVideoPlayerManager;

  private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

  private ListView mListView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mList = new ArrayList<>();
    users = new ArrayList<>();
    mListItemVisibilityCalculator
        = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mList);
    mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
      @Override
      public void onPlayerItemChanged(MetaData metaData) {
        if (SHOW_LOGS) Logger.v(TAG, "onPlayerItemChanged " + metaData);

      }
    });

    // if your files are in "assets" directory you can pass AssetFileDescriptor to the VideoPlayerView
    // if they are url's or path values you can pass the String path to the VideoPlayerView
    mList.add(ItemFactory.createItemFomDirectLink("Trip Italy 1",
        "http://146.169.46.220:8081/v.mp4",
        R.mipmap.video_sample_1_pic, getActivity(), mVideoPlayerManager,
            "Gicu Faimosu", "http://146.169.46.220:8081/1234.jpg"));

    mList.add(ItemFactory.createItemFomDirectLink("Trip Italy 2",
        "http://146.169.46.220:8081/v.mp4",
        R.mipmap.video_sample_1_pic, getActivity(), mVideoPlayerManager,
            "Marian Fatalu", "http://146.169.46.220:8081/1234.jpg"));

    View rootView = inflater.inflate(R.layout.fragment_video_list_view, container, false);

    mListView = (ListView) rootView.findViewById(R.id.list_view);
    VideoListViewAdapter videoListViewAdapter = new VideoListViewAdapter(mVideoPlayerManager, getActivity(), mList);
    mListView.setAdapter(videoListViewAdapter);

    mItemsPositionGetter = new ListViewItemPositionGetter(mListView);
    /**
     * We need to set onScrollListener after we create {@link #mItemsPositionGetter}
     * because {@link android.widget.AbsListView.OnScrollListener#onScroll(AbsListView, int, int, int)}
     * is called immediately and we will get {@link NullPointerException}
     */
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
          // on each scroll event we need to call onScroll for mListItemVisibilityCalculator
          // in order to recalculate the items visibility
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

  private void getNewsFeed(Response.Listener<String> listener) {
    StringRequest newsFeedRequest = new StringRequest(
        Request.Method.GET,
        NEWS_FEED_URL,
        listener,
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get newsfeed.");
          }
        });
    TripsterActivity.reqQ.add(newsFeedRequest);
  }

  private void processNewsFeedServerResponse(String response) {
    try {
      JSONArray newsfeedTripsArray = new JSONArray(response);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
