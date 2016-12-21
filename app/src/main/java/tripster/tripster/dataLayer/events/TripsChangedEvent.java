package tripster.tripster.dataLayer.events;

import com.couchbase.lite.LiveQuery;

public class TripsChangedEvent {
  private LiveQuery.ChangeEvent event;

  public TripsChangedEvent(LiveQuery.ChangeEvent event) {
    this.event = event;
  }

  public LiveQuery.ChangeEvent getEvent() {
    return event;
  }
}
