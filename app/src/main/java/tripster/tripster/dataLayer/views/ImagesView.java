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

import tripster.tripster.dataLayer.events.ImagesChangedEvent;

public class ImagesView {
  private static final String TAG = ImagesView.class.getName();
  private static final String IMAGES = "images";
  private static final String BY_PLACE = "byPlace";

  private View view;
  private LiveQuery liveQuery;

  public ImagesView(Database handle) {
    view = handle.getView(IMAGES + "/" + BY_PLACE);
    updateMapForView();
  }

  private void updateMapForView() {
    String mapVersion = UUID.randomUUID().toString();
    view.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if (document.containsKey("placeId")
            && document.containsKey("tripId")
            && document.containsKey("path")
            && document.containsKey("time")) {
          emitter.emit(document.get("placeId"), document);
        }
      }
    }, mapVersion);
  }

  public void startLiveQuery() {
    if (liveQuery == null) {
      liveQuery = view.createQuery().toLiveQuery();
      liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
        public void changed(final LiveQuery.ChangeEvent event) {
          EventBus.getDefault().postSticky(new ImagesChangedEvent(event));
        }
      });
    }
    liveQuery.start();
    Log.d(TAG, "We started the monitorization of images.");
  }

  public void stopLiveQuery() {
    if (liveQuery != null) {
      liveQuery.stop();
    }
    Log.d(TAG, "We stopped the monitorization of images.");
  }
}
