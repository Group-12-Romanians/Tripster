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

import tripster.tripster.dataLayer.events.PlacesChangedEvent;

public class PlacesView {
  private static final String TAG = ImagesView.class.getName();
  private static final String PLACES = "places";
  private static final String BY_TRIP = "byTrip";

  private View view;
  private LiveQuery liveQuery;

  public PlacesView(Database handle) {
    view = handle.getView(PLACES + "/" + BY_TRIP);
    updateMapForView();
  }

  private void updateMapForView() {
    String mapVersion = UUID.randomUUID().toString();
    view.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey("tripId")
            && document.containsKey("time")
            && document.containsKey("lat")
            && document.containsKey("lng")) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get("tripId"));
          keys.add(document.get("time"));
          emitter.emit(keys, document);
        }
      }
    }, mapVersion);
  }

  public void startLiveQuery() {
    if (liveQuery == null) {
      liveQuery = view.createQuery().toLiveQuery();
      liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
        public void changed(final LiveQuery.ChangeEvent event) {
          EventBus.getDefault().postSticky(new PlacesChangedEvent(event));
        }
      });
    }
    liveQuery.start();
    Log.d(TAG, "We started the monitorization of places.");
  }

  public void stopLiveQuery() {
    if (liveQuery != null) {
      liveQuery.stop();
    }
    Log.d(TAG, "We stopped the monitorization of places.");
  }

  public Document getLastLocationOfTrip(String tripId) {
    try {
      Query q = view.createQuery();
      List<Object> firstKey = new ArrayList<>();
      firstKey.add(tripId);
      firstKey.add((long) 0);
      List<Object> lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(System.currentTimeMillis());
      q.setStartKey(lastKey);
      q.setEndKey(firstKey);
      q.setDescending(true);
      QueryEnumerator enumerator = q.run();
      Document lastLocationDoc = null;
      for (QueryRow row : enumerator) {
        if (row.getDocument().getProperty("tripId").equals(tripId)) {
          lastLocationDoc = row.getDocument();
          break;
        }
      }
      return lastLocationDoc;
    } catch (CouchbaseLiteException e) {
      throw new RuntimeException("currently running trip query failed:" + e.getMessage());
    }
  }
}
