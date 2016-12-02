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
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.events.TripsChangedEvent;

public class TripsView {
  private static final String TAG = ImagesView.class.getName();
  private static final String TRIPS = "trips";
  private static final String BY_OWNER = "byOwner";

  private View view;
  private LiveQuery liveQuery;

  public TripsView(Database handle) {
    view = handle.getView(TRIPS + "/" + BY_OWNER);
    updateMapForView();
  }

  private void updateMapForView() {
    String mapVersion = UUID.randomUUID().toString();
    view.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey("ownerId")
            && document.containsKey("status")
            && document.containsKey("name")
            && document.containsKey("preview")) {
          emitter.emit(document.get("ownerId"), document);
        }
      }
    }, mapVersion);
  }

  public Document getCurrentlyRunningTrip(String ownerId) {
    try {
      Query q = view.createQuery();
      List<Object> keys = new ArrayList<>();
      keys.add(ownerId);
      q.setKeys(keys);
      QueryEnumerator enumerator = q.run();
      Document currentTripDoc = null;
      for (QueryRow row : enumerator) {
        if (row.getDocument().getProperty("status").equals("running")
            || row.getDocument().getProperty("status").equals("paused")) {
          if (currentTripDoc == null ) {
            currentTripDoc = row.getDocument();
          } else {
            Log.e(TAG, "Found one more running trip for this user");
          }
        }
      }
      return currentTripDoc;
    } catch (CouchbaseLiteException e) {
      throw new RuntimeException("currently running trip query failed:" + e.getMessage());
    }
  }

  public void startLiveQuery() {
    if (liveQuery == null) {
      liveQuery = view.createQuery().toLiveQuery();
      liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
        public void changed(final LiveQuery.ChangeEvent event) {
          EventBus.getDefault().postSticky(new TripsChangedEvent(event));
        }
      });
    }
    liveQuery.start();
    Log.d(TAG, "We started the monitorization of trips.");
  }

  public void stopLiveQuery() {
    if (liveQuery != null) {
      liveQuery.stop();
    }
    Log.d(TAG, "We stopped the monitorization of trips.");
  }
}
