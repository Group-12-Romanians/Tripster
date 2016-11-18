package tripster.tripster.trips;

import static tripster.tripster.TripsterActivity.SERVER_URL;

public class TripPreview {
  private String id;
  private String name;
  private String owner;
  private String previewPicture;
  private String previewVideo;

  public TripPreview(String id, String name, String previewPicture) {
    this.id = id;
    this.name = name;
    this.previewPicture = previewPicture;
  }

  public TripPreview(String tripId, String name, String owner, String previewImg, String previewVideo) {
    this.id = tripId;
    this.name = name;
    this.owner = owner;
    this.previewPicture = previewImg;
    this.previewVideo = previewVideo;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public String getPreviewURI() {
    return SERVER_URL + "/" + previewPicture + ".jpg";
  }

  public String getOwner() {
    return owner;
  }

  public String getPreviewVideo() {
    return SERVER_URL + "/" + previewVideo;
  }
}
