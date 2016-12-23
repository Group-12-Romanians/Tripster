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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tripster.tripster.Constants.DB_NAME;
import static tripster.tripster.Constants.DB_STORAGE_TYPE;
import static tripster.tripster.Constants.DB_SYNC_URL;
import static tripster.tripster.Constants.FRIENDSHIPS_BY_USER;
import static tripster.tripster.Constants.FRIENDS_BY_USER;
import static tripster.tripster.Constants.FS_LEVEL_CONFIRMED;
import static tripster.tripster.Constants.FS_LEVEL_K;
import static tripster.tripster.Constants.FS_LEVEL_SENT;
import static tripster.tripster.Constants.FS_RECEIVER_K;
import static tripster.tripster.Constants.FS_SENDER_K;
import static tripster.tripster.Constants.FS_TIME_K;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.NOTIFICATIONS_BY_USER;
import static tripster.tripster.Constants.PHOTO_PATH_K;
import static tripster.tripster.Constants.PHOTO_PLACE_K;
import static tripster.tripster.Constants.PHOTO_TIME_K;
import static tripster.tripster.Constants.PHOTO_TRIP_K;
import static tripster.tripster.Constants.PLACES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;
import static tripster.tripster.Constants.PLACE_TIME_K;
import static tripster.tripster.Constants.PLACE_TRIP_K;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_STATUS_K;
import static tripster.tripster.Constants.TRIP_STOPPED_AT_K;
import static tripster.tripster.Constants.USERS_BY_ID;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_EMAIL_K;
import static tripster.tripster.Constants.USER_NAME_K;

public class TripsterDb {
  private static final String TAG = TripsterDb.class.getName();

  private Database db;
  private static TripsterDb instance;

  public static TripsterDb getInstance(Context context) {
    if (instance == null) {
      instance = new TripsterDb(context);
    }
    return instance;
  }

  private TripsterDb(Context context) {
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
    if (props == null) {
      props = new HashMap<>();
    }
    for (String newK : newProps.keySet()) {
      Object newV = newProps.get(newK);
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
    initTripsByOwnerIdView();
    initPlacesByTripAndTimeView();
    initImagesByTripAndTimeView();
    initFriendsByUserIdView();
    initNotificationsByUser();
    initUsersById();
    initFriendshipsByUser();
  }

  private void initUsersById() {
    db.getView(USERS_BY_ID).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(USER_AVATAR_K)
            && document.containsKey(USER_EMAIL_K)
            && document.containsKey(USER_NAME_K)) {
          emitter.emit("0", document);
        }
      }
    }, "667"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initImagesByTripAndTimeView() {
    db.getView(IMAGES_BY_TRIP_AND_TIME).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(PHOTO_TRIP_K)
            && document.containsKey(PHOTO_TIME_K)
            && document.containsKey(PHOTO_PLACE_K)
            && document.containsKey(PHOTO_PATH_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(PHOTO_TRIP_K));
          keys.add(document.get(PHOTO_TIME_K));
          emitter.emit(keys, document.get(PHOTO_PLACE_K));
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initNotificationsByUser() {
    db.getView(NOTIFICATIONS_BY_USER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(FS_RECEIVER_K) &&
            document.containsKey(FS_LEVEL_K) &&
            document.get(FS_LEVEL_K).equals(FS_LEVEL_SENT)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(FS_RECEIVER_K));
          keys.add(document.get(FS_TIME_K));
          emitter.emit(keys, document); //NOTICE: I emit the document to catch changes in status
          //TODO: We might not need to emit document actually.
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initFriendshipsByUser() {
    db.getView(FRIENDSHIPS_BY_USER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(FS_RECEIVER_K) &&
            document.containsKey(FS_LEVEL_K) &&
            document.containsKey(FS_SENDER_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(FS_RECEIVER_K));
          keys.add(document.get(FS_SENDER_K));
          emitter.emit(keys, document); //NOTICE: I emit the document to catch changes in status
          //TODO: We might not need to emit document actually.
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initTripsByOwnerIdView() {
    db.getView(TRIPS_BY_OWNER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(TRIP_OWNER_K)
            && document.containsKey(TRIP_STATUS_K)
            && document.containsKey(TRIP_NAME_K)
            && document.containsKey(TRIP_PREVIEW_K)
            && document.containsKey(TRIP_STOPPED_AT_K)) {
          emitter.emit(document.get(TRIP_OWNER_K), document.get(TRIP_STOPPED_AT_K)); // NOTICE: emit this because we need to sort by it
          // Also note that this view does not see changes to trips, but that's never going to be needed (from my plans - Dragos)
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initFriendsByUserIdView() {
    db.getView(FRIENDS_BY_USER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(FS_SENDER_K)
            && document.containsKey(FS_RECEIVER_K)
            && document.containsKey(FS_LEVEL_K)) {
          if (document.get(FS_LEVEL_K).equals(FS_LEVEL_CONFIRMED)) {
            Map<String, Object> receiver = new HashMap<>();
            receiver.put("_id", document.get(FS_RECEIVER_K));
            emitter.emit(document.get(FS_SENDER_K), receiver);

            Map<String, Object> sender = new HashMap<>();
            sender.put("_id", document.get(FS_SENDER_K));
            emitter.emit(document.get(FS_RECEIVER_K), sender); // NOTICE: I redirect this to point at user rows because that's what we need anyway
            // Also this doesn't see changes in user docs and not even in friendship docs
          }
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  public void initPlacesByTripAndTimeView() {
    db.getView(PLACES_BY_TRIP_AND_TIME).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(PLACE_TRIP_K)
            && document.containsKey(PLACE_TIME_K)
            && document.containsKey(PLACE_LAT_K)
            && document.containsKey(PLACE_LNG_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(PLACE_TRIP_K));
          keys.add(document.get(PLACE_TIME_K));
          emitter.emit(keys, null);
        }
      }
    }, "666"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  public Document getLastLocationOfTrip(String tripId) {
    try {
      Query q = db.getExistingView(PLACES_BY_TRIP_AND_TIME).createQuery();
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
      if (enumerator.getCount() == 1) {
        return enumerator.getRow(0).getDocument();
      } else if (enumerator.getCount() == 0){
        return null;
      } else {
        Log.e(TAG, "Impossible, since limit was set to 1!!!!!!!");
        return null;
      }
    } catch (CouchbaseLiteException e) {
      throw new RuntimeException("Currently running trip query failed:" + e.getMessage());
    }
  }
}
