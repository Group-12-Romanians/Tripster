package tripster.tripster.UILayer.trip.editable;

import com.couchbase.lite.Document;

public class Place {

  private String id;
  private String name;
  private String description;

  public Place(Document placeDoc) {
    this.id = placeDoc.getId();
    this.name = (String) placeDoc.getProperty("name");
    this.description = (String) placeDoc.getProperty("description");
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
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
}
