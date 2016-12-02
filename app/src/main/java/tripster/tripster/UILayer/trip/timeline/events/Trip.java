package tripster.tripster.UILayer.trip.timeline.events;

import com.couchbase.lite.Document;

import java.util.List;

public class Trip {

  private String id;
  private String name;
  private String description;
  private List<Place> places;
  private List<ImageFromDoc> images;

  public Trip(Document trip, List<Place> places, List<ImageFromDoc> images) {
    this.id = trip.getId();
    this.name = (String) trip.getProperty("name");
    this.description = (String) trip.getProperty("description");
    this.places = places;
    this.images = images;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<Place> getPlaces() {
    return places;
  }

  public List<ImageFromDoc> getImages() {
    return images;
  }

  public void setImages(List<ImageFromDoc> images) {
    this.images = images;
  }
}
