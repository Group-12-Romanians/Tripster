package tripster.tripster.dataLayer.events;

import tripster.tripster.UILayer.trip.editable.Trip;

public class EditableTripEvent {
  private Trip trip;

  public EditableTripEvent(Trip trip) {
    this.trip = trip;
  }

  public Trip getTrip() {
    return trip;
  }
}
