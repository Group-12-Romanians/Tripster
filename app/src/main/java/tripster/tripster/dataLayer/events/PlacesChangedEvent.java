package tripster.tripster.dataLayer.events;

import com.couchbase.lite.LiveQuery;

public class PlacesChangedEvent {
  private LiveQuery.ChangeEvent event;

  public PlacesChangedEvent(LiveQuery.ChangeEvent event) {
    this.event = event;
  }

  public LiveQuery.ChangeEvent getEvent() {
    return event;
  }
}
