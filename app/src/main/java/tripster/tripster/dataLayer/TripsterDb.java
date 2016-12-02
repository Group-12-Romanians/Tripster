package tripster.tripster.dataLayer;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import tripster.tripster.dataLayer.exceptions.UninitialisedDatabaseException;
import tripster.tripster.dataLayer.views.FriendsView;
import tripster.tripster.dataLayer.views.ImagesView;
import tripster.tripster.dataLayer.views.PlacesView;
import tripster.tripster.dataLayer.views.TripsView;
import tripster.tripster.dataLayer.views.UsersView;

public class TripsterDb {
  private static final String TAG = TripsterDb.class.getName();
  private static final String DATABASE_NAME = "tripster";
  private static final String SERVER_URL = "http://146.169.46.220";
  private static final String DB_PORT = "6984";
  private static final String SYNC_URL = SERVER_URL + ":" + DB_PORT + "/" + DATABASE_NAME;

  private Manager manager;
  private Database handle;

  UsersView usersView;
  PlacesView placesView;
  TripsView tripsView;
  ImagesView imagesView;
  FriendsView friendsView;

  private static TripsterDb tripsterDb;

  public static TripsterDb getInstance() {
    if (tripsterDb != null) {
      return tripsterDb;
    }
    throw new UninitialisedDatabaseException("Trying to get handle to uninitialised database");
  }

  public static TripsterDb getInstance(Context context) {
    if (tripsterDb == null) {
      Log.d(TAG, "Creating new Instance of TripsterDB<><><><><><><><><><><><><><><><><><><><><><>");
      tripsterDb = new TripsterDb(context);
    }
    return tripsterDb;
  }

  private TripsterDb(Context context) {
    init(context);
    usersView = new UsersView(handle);
    placesView = new PlacesView(handle);
    tripsView = new TripsView(handle);
    imagesView = new ImagesView(handle);
    friendsView = new FriendsView(handle);
  }

  public Database getHandle() {
    return handle;
  }

  private void init(Context context) {
    try {
      manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
      manager.setStorageType("ForestDB");

      Manager.enableLogging(TAG, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG_QUERY, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG_VIEW, Log.VERBOSE);
      Manager.enableLogging(com.couchbase.lite.util.Log.TAG_DATABASE, Log.VERBOSE);

      handle = manager.getDatabase(DATABASE_NAME);
    } catch (Exception e) {
      Log.e(TAG, "Status???" + e.getMessage());
    }
  }

  public void pushChanges(final Response.Listener<String> listener) {
    URL syncUrl;

    try {
      syncUrl = new URL(SYNC_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Could not start Replication because of URL");
      return;
    }

    Replication pushReplication = handle.createPushReplication(syncUrl);
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
    URL syncUrl;

    try {
      syncUrl = new URL(SYNC_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Could not start Replication because of URL");
      return;
    }

    Replication pullReplication = handle.createPullReplication(syncUrl);
    pullReplication.setContinuous(true);

    Replication pushReplication = handle.createPushReplication(syncUrl);
    pushReplication.setContinuous(true);

    pullReplication.start();
    pushReplication.start();
  }

  public void upsertNewDocById(String id, Map<String, Object> props) {
    Document doc = handle.getDocument(id);
    Map<String, Object> prevProp = doc.getUserProperties();
    if (prevProp != null) {
      for (String k : prevProp.keySet()) {
        if (!props.containsKey(k)) {
          props.put(k, prevProp.get(k));
        }
      }
    }

    UnsavedRevision temp = doc.createRevision();
    temp.setUserProperties(props);
    try {
      temp.save();
    } catch (CouchbaseLiteException e) {
      Log.e(TAG, e.getCBLStatus().toString());
    }
  }

  public Document getCurrentlyRunningTrip(String ownerId) {
    return tripsView.getCurrentlyRunningTrip(ownerId);
  }

  public Document getLastLocationOfTrip(String tripId) {
    return placesView.getLastLocationOfTrip(tripId);
  }

  public void drop() {
    try {
      handle.delete();
      handle = manager.getDatabase(DATABASE_NAME);
    } catch (CouchbaseLiteException e) {
      Log.e(TAG, "Cannot delete db because: " + e.getMessage());
    }
  }

  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  public static void close() {
    if (tripsterDb != null) {
      tripsterDb.usersView.stopLiveQuery();
      tripsterDb.placesView.stopLiveQuery();
      tripsterDb.tripsView.stopLiveQuery();
      tripsterDb.imagesView.stopLiveQuery();
      tripsterDb.friendsView.stopLiveQuery();

      if (tripsterDb.manager != null) {
        tripsterDb.manager.close();
      }
      tripsterDb.manager = null;
      tripsterDb.handle = null;
      tripsterDb = null;
    }
  }

  public Document getUserWithId(String userId) {
    return usersView.getUserWithId(userId);
  }

  public void startUsersLiveQuery() {
    usersView.startLiveQuery();
  }

  public void startPlacesLiveQuery() {
    placesView.startLiveQuery();
  }

  public void startImagesLiveQuery() {
    imagesView.startLiveQuery();
  }

  public void startTripsLiveQuery() {
    tripsView.startLiveQuery();
  }

  public void startFriendsLiveQuery() {
    friendsView.startLiveQuery();
  }
}
