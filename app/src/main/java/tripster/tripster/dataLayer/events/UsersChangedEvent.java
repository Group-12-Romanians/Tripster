package tripster.tripster.dataLayer.events;

import com.couchbase.lite.LiveQuery;

public class UsersChangedEvent {
  private LiveQuery.ChangeEvent event;

  public UsersChangedEvent(LiveQuery.ChangeEvent event) {
    this.event = event;
  }

  public LiveQuery.ChangeEvent getEvent() {
    return event;
  }
}
