package tripster.tripster;

public class Trip {
  private String id;
  private String name;
  private String previewPicture;

  public Trip(String id, String name, String previewPicture) {
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
