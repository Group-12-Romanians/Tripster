package tripster.tripster.dataLayer.views;

import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.events.FriendsChangedEvent;

public class FriendsView {
  private static final String TAG = FriendsView.class.getName();
  private static final String FRIENDS = "friends";
  private static final String BY_LEVEL = "byLevel";

  private View view;
  private LiveQuery liveQuery;

  public FriendsView(Database handle) {
    view = handle.getView(FRIENDS + "/" + BY_LEVEL);
    updateMapForView();
  }

  private void updateMapForView() {
    String mapVersion = UUID.randomUUID().toString();
    view.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey("level")
            && document.containsKey("sender")
            && document.containsKey("receiver")) {
          emitter.emit(document.get("level"), document);
        }
      }
    }, mapVersion);
  }

  public void startLiveQuery() {
    if (liveQuery == null) {
      liveQuery = view.createQuery().toLiveQuery();
      liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
        public void changed(final LiveQuery.ChangeEvent event) {
          EventBus.getDefault().postSticky(new FriendsChangedEvent(event));
        }
      });
    }
    liveQuery.start();
    Log.d(TAG, "We started the monitorization of friends.");
  }

  public void stopLiveQuery() {
    if (liveQuery != null) {
      liveQuery.stop();
    }
    Log.d(TAG, "We stopped the monitorization of friends.");
  }
}
