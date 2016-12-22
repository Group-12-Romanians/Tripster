package tripster.tripster.dataLayer.events;

import tripster.tripster.UILayer.trip.timeline.models.Trip;

public class EditableTripEvent {
  private Trip trip;

  public EditableTripEvent(Trip trip) {
    this.trip = trip;
  }

  public Trip getTrip() {
    return trip;
  }
}
