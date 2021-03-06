package tripster.tripster.UILayer.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;

import static tripster.tripster.Constants.NOTIFICATIONS_BY_USER;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class NotificationsFragment extends Fragment {
  private static final String TAG = NotificationsFragment.class.getName();

  LiveQuery notificationsLQ;

  private ListView notifications;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_notifications, container, false);
    notifications = (ListView) v.findViewById(R.id.notifications_list);
    return v;
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
    lastKey.add(Long.MAX_VALUE);
    q.setStartKey(lastKey);
    q.setEndKey(firstKey);
    q.setDescending(true);
    q.setMapOnly(true);

    notificationsLQ = q.toLiveQuery();
    notificationsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<String> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          results.add(event.getRows().getRow(i).getDocumentId());
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            initNotificationsAdapter(results);
          }
        });
      }
    });
    notificationsLQ.start();
  }

  @Override
  public void onPause() {
    try {
      notificationsLQ.stop();
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }

  private void initNotificationsAdapter(final List<String> nots) {
    NotificationsAdapter notificationsAdapter = new NotificationsAdapter(getContext(),
        R.layout.fragment_notifications,
        R.id.notification_text,
        nots);
    notifications.setAdapter(notificationsAdapter);
  }
}
