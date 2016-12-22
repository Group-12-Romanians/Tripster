package tripster.tripster.UILayer.trip.editable;

import com.couchbase.lite.Document;

public class ImageFromDoc {
  private String id;
  private String description;
  private boolean deleted;
  private String path;

  public ImageFromDoc(Document imageDoc) {
    this.description = (String) imageDoc.getProperty("description");
    this.path = (String) imageDoc.getProperty("path");
    this.id = imageDoc.getId();
    deleted = false;
  }

  public String getId() {
    return id;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getPath() {
    return path;
  }
}
