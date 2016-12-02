package tripster.tripster.UILayer.notifications;

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
import tripster.tripster.dataLayer.events.UsersChangedEvent;

public class NotificationsFragment extends Fragment {
  private static final String TAG = NotificationsFragment.class.getName();
  private NotificationsAdapter notificationsAdapter;
  private ListView notificationsList;
  private Map<String, Document> users;
  private Set<Document> myNotifications;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_requests, container, false);
    notificationsList = (ListView) view.findViewById(R.id.notifications_list);
    return view;
  }

  @Override
  public void onResume() {
    users = new HashMap<>();
    myNotifications = new HashSet<>();
    EventBus.getDefault().register(this);
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "UnRegister fragment");
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onUsersChangedEvent(UsersChangedEvent event) {
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    for (int i = 0; i < changes.getCount(); i++) {
      Document doc = changes.getRow(i).getDocument();
      users.put(doc.getId(), doc);
    }
    initNotificationsAdapter();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onFriendsChangedEvent(FriendsChangedEvent event) {
    myNotifications.clear();
    Log.d(TAG, "Search for requests");
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    for (int i = 0; i < changes.getCount(); i++) {
      Log.d(TAG, "found some changes");
      if (changes.getRow(i).getDocument().getProperty("level").equals("sent")) {
        Document doc = changes.getRow(i).getDocument();
        Log.d(TAG, "Doc is:" + doc.getProperty("sender"));
        if (doc.getProperty("receiver").equals(TripsterActivity.USER_ID)) {
          Log.d(TAG, "Got a friend request yuhuu");
          myNotifications.add(doc);
        }
      }
    }
    initNotificationsAdapter();
  }

  private void initNotificationsAdapter() {
    List<Pair<String, Document>> list = new ArrayList<>();
    for (Document notification : myNotifications) {
      Document sender = users.get(notification.getProperty("sender"));

      list.add(new Pair<>(notification.getId(),sender));
    }

    notificationsAdapter = new NotificationsAdapter(getContext(),
        R.layout.fragment_requests,
        R.id.notification_text,
        list);
    notificationsList.setAdapter(notificationsAdapter);
  }

}
