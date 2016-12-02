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

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tripster.tripster.R;
import tripster.tripster.UILayer.TripsterActivity;
import tripster.tripster.dataLayer.events.FriendsChangedEvent;
import tripster.tripster.dataLayer.events.TripsChangedEvent;
import tripster.tripster.dataLayer.events.UsersChangedEvent;

public class NewsfeedFragment extends Fragment {
  private static final String TAG = NewsfeedFragment.class.getName();

  private NewsfeedAdapter newsfeedAdapter;
  private Map<String, Document> allUsers;
  private Set<String> friendsIds;
  private Map<String,Set<Document>> trips;
  private ListView newsfeed;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
    newsfeed = (ListView) view.findViewById(R.id.userStories);
    allUsers = new HashMap<>();
    friendsIds = new HashSet<>();
    trips = new HashMap<>();
    return view;
  }

  @Override
  public void onResume() {
    Log.d(TAG, "Register fragment");
    EventBus.getDefault().register(this);
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "Unregister fragment");
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  private void initItemListAdapter() {
    List<Pair<Document, Document>> userStories = new ArrayList<>();

    for (String friendId : friendsIds) {
      if (trips.containsKey(friendId)) {
        for (Document tripDoc : trips.get(friendId)) {
          userStories.add(new Pair<>(allUsers.get(friendId), tripDoc));
        }
      }
    }

    newsfeedAdapter = new NewsfeedAdapter(
        getActivity(),
        R.layout.user_story,
        R.id.tripDescription,
       userStories);
    newsfeed.setAdapter(newsfeedAdapter);
  }

  //-----------------------------EVENTS--------------------------------------//
  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onUsersChangedEvent(UsersChangedEvent event) {
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    for (int i = 0; i < changes.getCount(); i++) {
      Document doc = changes.getRow(i).getDocument();
      allUsers.put((doc.getId()), doc);
    }
    initItemListAdapter();
  }


  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onTripsChangedEvent(TripsChangedEvent event) {
    Log.d(TAG, "In trips change");
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    boolean changed = false;
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      Document tripDoc = row.getDocument();
      // Get only the documents corresponding to friends' trips.
      String ownerId = (String) tripDoc.getProperty("ownerId");
      if (tripDoc.getProperty("status").equals("stopped")) {
        if (!trips.containsKey(ownerId)) {
          Set<Document> docs = new HashSet<>();
          docs.add(tripDoc);
          trips.put(ownerId, docs);
          changed = true;
        } else {
          if (trips.get(ownerId).add(tripDoc)) {
            changed = true;
          }
        }
      }
    }

    if (changed) {
      initItemListAdapter();
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onFriendsChangedEvent(FriendsChangedEvent event) {
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    boolean changed = false;
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      if (row.getKey().equals("confirmed")) {
        Document friendshipDoc = row.getDocument();
        if (friendshipDoc.getProperty("sender").equals(TripsterActivity.USER_ID)) {
          if (friendsIds.add((String) friendshipDoc.getProperty("receiver"))) {
           changed = true;
          }
        } else if (friendshipDoc.getProperty("receiver").equals(TripsterActivity.USER_ID)) {
          if (friendsIds.add((String) friendshipDoc.getProperty("sender"))) {
            changed = true;
          }
        }
      }
    }
    if (changed) {
      initItemListAdapter();
    }
  }
}
