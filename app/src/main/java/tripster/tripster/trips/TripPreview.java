package tripster.tripster.trips;

public class TripPreview {
  private String id;
  private String name;
  private String previewPicture;

  public TripPreview(String id, String name, String previewPicture) {
    this.id = id;
    this.name = name;
    this.previewPicture = previewPicture;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public String getPreviewURI() {
    return previewPicture;
  }
}
