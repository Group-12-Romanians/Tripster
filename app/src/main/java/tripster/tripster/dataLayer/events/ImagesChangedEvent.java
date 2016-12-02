package tripster.tripster.dataLayer.events;

import com.couchbase.lite.LiveQuery;

public class ImagesChangedEvent {
  private LiveQuery.ChangeEvent event;
  public ImagesChangedEvent(LiveQuery.ChangeEvent event) {
    this.event = event;
  }

  public LiveQuery.ChangeEvent getEvent() {
    return event;
  }
}
