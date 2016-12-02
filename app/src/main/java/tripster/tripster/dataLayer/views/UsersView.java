package tripster.tripster.dataLayer.views;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.events.UsersChangedEvent;
import tripster.tripster.dataLayer.exceptions.TooManyUsersWithOneIdException;

public class UsersView {
  private static final String TAG = UsersView.class.getName();
  private static final String USERS = "users";
  private static final String BY_ID = "byId";

  private View view;
  private LiveQuery liveQuery;

  public UsersView(Database handle) {
    view = handle.getView(USERS + "/" + BY_ID);
    updateMapForView();
  }

  private void updateMapForView() {
    String mapVersion = UUID.randomUUID().toString();
    view.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey("email")
            && document.containsKey("avatarUrl")
            && document.containsKey("name")) {
          emitter.emit(document, document);
        }
      }
    }, mapVersion);
  }

  public void startLiveQuery() {
    if (liveQuery == null) {
      liveQuery = view.createQuery().toLiveQuery();
      liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
        public void changed(final LiveQuery.ChangeEvent event) {
          EventBus.getDefault().postSticky(new UsersChangedEvent(event));
        }
      });
    }
    liveQuery.start();
    Log.d(TAG, "We started the monitorization of users.");
  }

  public void stopLiveQuery() {
    if (liveQuery != null) {
      liveQuery.stop();
    }
    Log.d(TAG, "We stopped the monitorization of users.");
  }

  public Document getUserWithId(String userId) {
    try {
      Query q = view.createQuery();
      List<Object> key = new ArrayList<>();
      key.add(userId);
      List<Object> keys = new ArrayList<>();
      keys.add(key);
      q.setKeys(keys);
      QueryEnumerator enumerator = q.run();
      if(enumerator.getCount() == 0) {
        return null;
      } else if(enumerator.getCount() == 1) {
        return enumerator.getRow(0).getDocument();
      } else {
        throw new TooManyUsersWithOneIdException("No users with id: " + userId);
      }
    } catch (CouchbaseLiteException e) {
      throw new RuntimeException("getUserWithId query failed:" + e.getMessage());
    }
  }
}


