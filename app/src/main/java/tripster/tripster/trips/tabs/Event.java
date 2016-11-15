package tripster.tripster.trips.tabs;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tripster.tripster.TripsterActivity.SERVER_URL;

public class Event implements Parcelable {
  private long timeStamp;
  private double lat;
  private double lng;
  private List<String> photoUris;

  public Event(long timeStamp, double lat, double lng) {
    this.timeStamp = timeStamp;
    this.lat = lat;
    this.lng = lng;
    this.photoUris = new ArrayList<>();
  }

  public Event(long timeStamp, double lat, double lng, List<String> photoUris) {
    this.timeStamp = timeStamp;
    this.lat = lat;
    this.lng = lng;
    this.photoUris = new ArrayList<>();
    for (String uri : photoUris) {
      this.photoUris.add(completeUri(uri));
    }
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public List<String> getPhotoUris() {
    return photoUris;
  }

  public void addPhotoUri(String photoUri) {

    this.photoUris.add(completeUri(photoUri));
  }

  @Override
  public String toString() {
    return "at " + timeStamp + " Event with lat: " + lat + ", lng: " + lng + " and imgs: " + photoUris.toString();
  }

  public LatLng getLocation() {
    return new LatLng(lat, lng);
  }

  private String completeUri(String photoUri) {
    if (photoUri.contains("/")) {
      return photoUri;
    }
    return SERVER_URL + "/" + photoUri + ".jpg";
  }

  // Parcelling part
  private Event(Parcel in){
    this.timeStamp = in.readLong();
    this.lat = in.readDouble();
    this.lng = in.readDouble();
    this.photoUris = Arrays.asList(in.readString().split(","));
  }

  @Override
  public int describeContents(){
    return hashCode();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(timeStamp);
    dest.writeDouble(lat);
    dest.writeDouble(lng);

    StringBuilder sb = new StringBuilder();
    String prefix = "";
    for(String uri : photoUris) {
      sb.append(prefix);
      prefix = ",";
      sb.append(uri);
    }
    dest.writeString(sb.toString());
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public Event createFromParcel(Parcel in) {
      return new Event(in);
    }

    public Event[] newArray(int size) {
      return new Event[size];
    }
  };
}
