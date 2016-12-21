package tripster.tripster.dataLayer.events;

import com.couchbase.lite.LiveQuery;

public class FriendsChangedEvent {
  private LiveQuery.ChangeEvent event;

  public FriendsChangedEvent(LiveQuery.ChangeEvent event) {
    this.event = event;
  }

  public LiveQuery.ChangeEvent getEvent() {
    return event;
  }
}
