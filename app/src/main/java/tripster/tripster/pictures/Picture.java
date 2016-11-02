package tripster.tripster.pictures;

public class Picture {

  private String latitude;
  private String longitude;
  private String pathToPhoto;

  private long dateTaken;

  public Picture(long dateTaken, String latitude, String longitude, String pathToPhoto) {
    this.dateTaken = dateTaken;
    this.latitude = latitude;
    this.longitude = longitude;
    this.pathToPhoto = pathToPhoto;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getPathToPhoto() {
    return pathToPhoto;
  }

  public String getLongitude() {
    return longitude;
  }

  public long getDateTaken() {
    return dateTaken;
  }

  @Override
  public String toString() {
    return pathToPhoto + ", latitude: " + latitude + ", logitude: " + longitude + "date: " + dateTaken;
  }
}
