package tripster.tripster.dataLayer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Response;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static tripster.tripster.Constants.DB_NAME;
import static tripster.tripster.Constants.DB_STORAGE_TYPE;
import static tripster.tripster.Constants.DB_SYNC_URL;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_STATUS_K;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.TRIP_STOPPED_AT;

public class TripsterDb {
  private static final String TAG = TripsterDb.class.getName();

  private Database db;

  public TripsterDb(Context context) {
    try {
      Manager manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
      manager.setStorageType(DB_STORAGE_TYPE);

      db = manager.getDatabase(DB_NAME);
    } catch (Exception e) {
      Log.e(TAG, "DB Status:" + e.getMessage());
    }
  }

  public Database getDb() {
    return db;
  }

  public Document getDocumentById(String id) {
    return db.getExistingDocument(id);
  }

  public void pushChanges(final Response.Listener<String> listener) {
    URL syncUrl = getSyncUrl();
    if (syncUrl == null) return;

    Replication pushReplication = db.createPushReplication(syncUrl);
    pushReplication.addChangeListener(new Replication.ChangeListener() {
      @Override
      public void changed(Replication.ChangeEvent event) {
        if (event.getTransition() != null) {
          if (event.getTransition().getDestination().equals(ReplicationState.STOPPED)) {
            listener.onResponse("stopped");
          } else if (event.getTransition().getDestination().equals(ReplicationState.OFFLINE)) {
            listener.onResponse("offline");
          }
        }
      }
    });
    pushReplication.start();
  }

  public void startSync() {
    startPullSync();
    startPushSync();
  }

  public void startPullSync() {
    URL syncUrl = getSyncUrl();
    if (syncUrl == null) return;

    Replication pullReplication = db.createPullReplication(syncUrl);
    pullReplication.setContinuous(true);
    pullReplication.start();
  }

  public void startPushSync() {
    URL syncUrl = getSyncUrl();
    if (syncUrl == null) return;

    Replication pushReplication = db.createPushReplication(syncUrl);
    pushReplication.setContinuous(true);
    pushReplication.start();
  }

  public void upsertNewDocById(String id, Map<String, Object> newProps) {
    Document doc = db.getDocument(id);
    boolean changed = false;
    Map<String, Object> props = doc.getUserProperties();
    for (String newK : newProps.keySet()) {
      String newV = (String) newProps.get(newK);
      if (!(props.containsKey(newK) && props.get(newK).equals(newV))) {
        props.put(newK, newV);
        changed = true;
      }
    }

    if (changed) {
      UnsavedRevision temp = doc.createRevision();
      temp.setUserProperties(props);
      try {
        temp.save();
      } catch (CouchbaseLiteException e) {
        Log.e(TAG, e.toString());
      }
    }
  }

  @Nullable
  private URL getSyncUrl() {
    URL syncUrl;

    try {
      syncUrl = new URL(DB_SYNC_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Could not start Replication because of URL");
      return null;
    }
    return syncUrl;
  }

  public void initAllViews() {
    String mapVersion = UUID.randomUUID().toString();
    db.getView(TRIPS_BY_OWNER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(TRIP_OWNER_K)
            && document.containsKey(TRIP_STATUS_K)
            && document.containsKey(TRIP_NAME_K)
            && document.containsKey(TRIP_PREVIEW_K)
            && document.containsKey(TRIP_STOPPED_AT)) {
          emitter.emit(document.get(TRIP_OWNER_K), null);
        }
      }
    }, mapVersion);

//    db.getView("users/byId").setMap(new Mapper() {
//      @Override
//      public void map(Map<String, Object> document, Emitter emitter) {
//        if (document.containsKey("email")
//            && document.containsKey("avatarUrl")
//            && document.containsKey("name")) {
//          emitter.emit(document.get("_id"), null);
//        }
//      }
//    }, mapVersion);
//
//    db.getView("places/byTripAndTime").setMap(new Mapper() {
//      @Override
//      public void map(Map<String, Object> document, Emitter emitter) {
//        if (document.containsKey("tripId")
//            && document.containsKey("time")
//            && document.containsKey("lat")
//            && document.containsKey("lng")) {
//          List<Object> keys = new ArrayList<>();
//          keys.add(document.get("tripId"));
//          keys.add(document.get("time"));
//          emitter.emit(keys, document);
//        }
//      }
//    }, mapVersion);
//
//    db.getView("images/byPlace").setMap(new Mapper() {
//      @Override
//      public void map(Map<String, Object> document, Emitter emitter) {
//        if (document.containsKey("placeId")
//            && document.containsKey("tripId")
//            && document.containsKey("path")
//            && document.containsKey("time")) {
//          emitter.emit(document.get("placeId"), document);
//        }
//      }
//    }, mapVersion);
//
//    db.getView("friendships/byLevel").setMap(new Mapper() {
//      @Override
//      public void map(Map<String, Object> document, Emitter emitter) {
//        if (document.containsKey("level")
//            && document.containsKey("sender")
//            && document.containsKey("receiver")) {
//          emitter.emit(document.get("level"), document);
//        }
//      }
//    }, mapVersion);
  }

  public Document getLastLocationOfTrip(String tripId) {
    try {
      Query q = db.getExistingView("places/byTripAndTime").createQuery();
      List<Object> firstKey = new ArrayList<>();
      firstKey.add(tripId);
      firstKey.add((long) 0);
      List<Object> lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(System.currentTimeMillis());
      q.setStartKey(lastKey);
      q.setEndKey(firstKey);
      q.setDescending(true);
      q.setLimit(1);
      QueryEnumerator enumerator = q.run();

      return enumerator.getRow(0).getDocument();
    } catch (CouchbaseLiteException e) {
      throw new RuntimeException("currently running trip query failed:" + e.getMessage());
    }
  }
}
