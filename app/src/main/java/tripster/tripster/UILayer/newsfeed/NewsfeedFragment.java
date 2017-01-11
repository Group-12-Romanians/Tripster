package tripster.tripster.UILayer.newsfeed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;

import static tripster.tripster.Constants.FOLLOWING_BY_USER;
import static tripster.tripster.Constants.LEVEL_PUBLIC;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.TRIP_STOPPED_AT_K;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class NewsfeedFragment extends Fragment {
  private static final String TAG = NewsfeedFragment.class.getName();

  private ListView stories;

  private LiveQuery followingLQ;
  private LiveQuery fTripsLQ;

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);
    stories = (ListView) v.findViewById(R.id.userStories);
    return v;
  }

  @Override
  public void onResume() {
    super.onResume();
    restartFollowingLiveQuery();
  }

  private void restartFollowingLiveQuery() {
    Query q = tDb.getDb().getExistingView(FOLLOWING_BY_USER).createQuery();
    q.setKeys(Collections.<Object>singletonList(currentUserId));
    q.setMapOnly(true);

    followingLQ = q.toLiveQuery();
    followingLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        Map<String, Integer> followingLevels = new HashMap<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          String followingId = r.getDocumentId().split(":")[1];
          followingLevels.put(followingId, (Integer) r.getValue());
        }
        if (followingLevels.size() > 0) {
          restartTripsLiveQuery(followingLevels);
        }
      }
    });
    followingLQ.start();
  }

  private void restartTripsLiveQuery(final Map<String, Integer> followingLevels) {
    Query q = tDb.getDb().getExistingView(TRIPS_BY_OWNER).createQuery();
    q.setKeys(new ArrayList<Object>(followingLevels.keySet()));

    fTripsLQ = q.toLiveQuery();
    fTripsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<Pair<Long, String>> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          String ownerId = (String) r.getKey();
          if ((Integer) r.getValue() <= Math.max(followingLevels.get(ownerId), LEVEL_PUBLIC)) {
            Document d = r.getDocument();
            Long stoppedAt = (Long) d.getProperty(TRIP_STOPPED_AT_K);
            if (stoppedAt != null) {
              results.add(new Pair<>(stoppedAt, d.getId()));
            }
          }
        }
        Collections.sort(results, new Comparator<Pair<Long, String>>() {
          @Override
          public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
            return o2.first.compareTo(o1.first);
          }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            initItemListAdapter(results);
          }
        });
      }
    });
    fTripsLQ.start();
  }

  @Override
  public void onPause() {
    try {
      followingLQ.stop();
      fTripsLQ.stop();
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }

  private void initItemListAdapter(List<Pair<Long, String>> results) {
    List<String> userStories = new ArrayList<>();
    for (Pair<Long, String> p : results) {
      userStories.add(p.second);
    }
    NewsfeedAdapter newsfeedAdapter = new NewsfeedAdapter(getActivity(), userStories);
    stories.setAdapter(newsfeedAdapter);
  }
}