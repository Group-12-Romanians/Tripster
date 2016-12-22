package tripster.tripster.UILayer.newsfeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.FRIENDS_BY_USER;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class NewsfeedFragment extends Fragment {
  private static final String TAG = NewsfeedFragment.class.getName();

  private LiveQuery friendsLQ;
  private LiveQuery fTripsLQ;

  private Set<String> friends = new HashSet<>();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_newsfeed, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    restartFriendsLiveQuery();
  }

  private void restartFriendsLiveQuery() {
    Query q = tDb.getDb().getExistingView(FRIENDS_BY_USER).createQuery();
    q.setKeys(Collections.singletonList((Object) currentUserId));
    friendsLQ = q.toLiveQuery();
    friendsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        boolean changed = false;
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          changed = changed || friends.add(r.getDocumentId());
        }
        if (changed) {
          restartTripsLiveQuery();
        }
      }
    });
    friendsLQ.start();
  }

  private void restartTripsLiveQuery() {
    Query q = tDb.getDb().getExistingView(TRIPS_BY_OWNER).createQuery();
    q.setKeys(new ArrayList<Object>(friends));
    fTripsLQ = q.toLiveQuery();
    fTripsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<Pair<Long, String>> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          Pair<Long, String> p = new Pair<>((Long) r.getValue(), r.getDocumentId());
          results.add(p);
        }
        Collections.sort(results, new Comparator<Pair<Long, String>>() {
          @Override
          public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
            return o2.first.compareTo(o1.first);
          }
        });
        initItemListAdapter(results);
      }
    });
    fTripsLQ.start();
  }

  @Override
  public void onPause() {
    try {
      friendsLQ.stop();
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

    NewsfeedAdapter newsfeedAdapter = new NewsfeedAdapter(
        getActivity(),
        R.layout.user_story,
        R.id.tripDescription,
        userStories);
    assertNotNull(getView());
    ((ListView) getView().findViewById(R.id.userStories)).setAdapter(newsfeedAdapter);
  }
}