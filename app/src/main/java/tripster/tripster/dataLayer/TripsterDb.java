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
import com.couchbase.lite.Reducer;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
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
import static tripster.tripster.Constants.DOC_ID;
import static tripster.tripster.Constants.FOLLOWERS_BY_USER;
import static tripster.tripster.Constants.FOLLOWING_BY_USER;
import static tripster.tripster.Constants.FOL_LEVEL_K;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.Constants.LOCATIONS_BY_TRIP;
import static tripster.tripster.Constants.NOTIFICATIONS_BY_USER;
import static tripster.tripster.Constants.NOT_RECEIVER_K;
import static tripster.tripster.Constants.NOT_TIME_K;
import static tripster.tripster.Constants.NOT_TYPE_K;
import static tripster.tripster.Constants.PHOTO_PLACE_K;
import static tripster.tripster.Constants.PHOTO_TIME_K;
import static tripster.tripster.Constants.PHOTO_TRIP_K;
import static tripster.tripster.Constants.PLACES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;
import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.Constants.PLACE_TIME_K;
import static tripster.tripster.Constants.PLACE_TRIP_K;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.TRIP_LEVEL_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
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
    initUsersById();
    initImagesByTripAndPlace();
    initTripsByOwnerIdView();
    initImportantPlacesByTripAndTime();
    initLocationsByTrip();
    initFollowingByUser();
    initFollowersByUser();
    initNotificationsByUser();
  }

  private void initLocationsByTrip() {
    db.getView(LOCATIONS_BY_TRIP).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(PLACE_TIME_K)
            && document.containsKey(PLACE_LNG_K)
            && document.containsKey(PLACE_LAT_K)
            && document.containsKey(PLACE_TRIP_K)) {
          emitter.emit(document.get(PLACE_TRIP_K), null); // we need to sort by it
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initImportantPlacesByTripAndTime() {
    db.getView(PLACES_BY_TRIP_AND_TIME).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(PLACE_LAT_K)
            && document.containsKey(PLACE_LNG_K)
            && document.containsKey(PLACE_TIME_K)
            && document.containsKey(PLACE_TRIP_K)
            && document.containsKey(PLACE_NAME_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(PLACE_TRIP_K));
          keys.add(document.get(PLACE_TIME_K));
          emitter.emit(keys, null);
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initUsersById() {
    db.getView(USERS_BY_ID).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(USER_AVATAR_K)
            && document.containsKey(USER_EMAIL_K)
            && document.containsKey(USER_NAME_K)) {
          emitter.emit("0", null); // we only catch additions here (no need to see changes in users)
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initImagesByTripAndPlace() {
    db.getView(IMAGES_BY_TRIP_AND_PLACE).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(PHOTO_TRIP_K)
            && document.containsKey(PHOTO_TIME_K)
            && document.containsKey(PHOTO_PLACE_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(PHOTO_TRIP_K));
          keys.add(document.get(PHOTO_PLACE_K));
          emitter.emit(keys, document.get(PHOTO_TIME_K)); // we need to sort by it
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initTripsByOwnerIdView() {
    db.getView(TRIPS_BY_OWNER).setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(TRIP_OWNER_K) && document.containsKey(TRIP_LEVEL_K)) {
          emitter.emit(document.get(TRIP_OWNER_K), document.get(TRIP_LEVEL_K)); // emit this because we need to filter by it
          // This only sees additions and changes in trip levels
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initFollowingByUser() {
    db.getView(FOLLOWING_BY_USER).setMapReduce(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        String docId = ((String) document.get(DOC_ID)); // The id is of type: followerId:followingId
        if (document.containsKey(FOL_LEVEL_K) && docId.contains(":")) {
          emitter.emit(docId.split(":")[0], document.get(FOL_LEVEL_K)); //changes in level for newsfeed
        }
      }
    }, new Reducer() {
      @Override
      public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) { // count number of subjects
        if (rereduce) {
          return View.totalValues(values);
        } else {
          return values.size();
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initFollowersByUser() {
    db.getView(FOLLOWERS_BY_USER).setMapReduce(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        String docId = ((String) document.get(DOC_ID)); // The id is of type: followerId:followingId
        if (document.containsKey(FOL_LEVEL_K) && docId.contains(":")) {
          emitter.emit(docId.split(":")[1], null); // second part of docId is the subject of the following
        }
      }
    }, new Reducer() {
      @Override
      public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) { //count number of followers
        if (rereduce) {
          return View.totalValues(values);
        } else {
          return values.size();
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }

  private void initNotificationsByUser() {
    db.getView(NOTIFICATIONS_BY_USER).setMapReduce(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey(NOT_RECEIVER_K)
            && document.containsKey(NOT_TIME_K)
            && document.containsKey(NOT_TYPE_K)) {
          List<Object> keys = new ArrayList<>();
          keys.add(document.get(NOT_RECEIVER_K));
          keys.add(document.get(NOT_TIME_K));
          emitter.emit(keys, null);
        }
      }
    }, new Reducer() {
      @Override
      public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) { //count number of followers
        if (rereduce) {
          return View.totalValues(values);
        } else {
          return values.size();
        }
      }
    }, "700"); //ATTENTION!!!!!!!!!!!!!!! When changing the code of map also increment this number.
  }
}
