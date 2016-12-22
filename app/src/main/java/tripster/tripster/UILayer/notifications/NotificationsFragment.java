package tripster.tripster.UILayer.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.NOTIFICATIONS_BY_USER;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class NotificationsFragment extends Fragment {
  private static final String TAG = NotificationsFragment.class.getName();
  LiveQuery notificationsLQ;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_requests, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    restartNotificationsLiveQuery();
  }

  private void restartNotificationsLiveQuery() {
    Query q = tDb.getDb().getExistingView(NOTIFICATIONS_BY_USER).createQuery();
    List<Object> firstKey = new ArrayList<>();
    firstKey.add(currentUserId);
    firstKey.add((long) 0);
    List<Object> lastKey = new ArrayList<>();
    lastKey.add(currentUserId);
    lastKey.add(System.currentTimeMillis());
    q.setStartKey(lastKey);
    q.setEndKey(firstKey);
    q.setDescending(true);
    notificationsLQ = q.toLiveQuery();
    notificationsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          results.add(r.getDocumentId());
        }
        initNotificationsAdapter(results);
      }
    });
    notificationsLQ.start();
  }

  @Override
  public void onPause() {
    notificationsLQ.stop();
    super.onPause();
  }

  private void initNotificationsAdapter(List<String> friendRequests) {
    NotificationsAdapter notificationsAdapter = new NotificationsAdapter(getContext(),
        R.layout.fragment_requests,
        R.id.notification_text,
        friendRequests);
    assertNotNull(getView());
    ((ListView) getView().findViewById(R.id.notifications_list)).setAdapter(notificationsAdapter);
  }

}
