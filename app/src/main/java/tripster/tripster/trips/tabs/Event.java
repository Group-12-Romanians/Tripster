package tripster.tripster.trips.tabs;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tripster.tripster.TripsterActivity;

import static tripster.tripster.TripsterActivity.MAPS_OPT;
import static tripster.tripster.TripsterActivity.MAPS_URL;
import static tripster.tripster.TripsterActivity.SERVER_URL;

public class Event implements Parcelable {
    private final static String TAG = Event.class.getName();

    private long timeStamp;
    private double lat;
    private double lng;
    private List<String> photoUris;
    private String place = "";

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

    public void onPlaceFound(final Response.Listener<String> listener) {
        if (place.isEmpty()) {
            setPlaceName(listener);
        } else {
            listener.onResponse(place);
        }
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

    private void setPlaceName(final Response.Listener<String> listener) {
        String url = MAPS_URL + "/json?location=" + lat + "," + lng + MAPS_OPT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "Place name response is: " + response);

                            JSONObject respJSON = new JSONObject(response);
                            JSONArray resultsJSON = respJSON.getJSONArray("results");
                            if (!respJSON.getString("status").equals("ZERO_RESULTS")) {
                                // The first result in the JSON is the street name. The second is the
                                // actual place name.
                                int position = resultsJSON.length() > 1 ? 1 : 0;
                                place = resultsJSON.getJSONObject(position).getString("name");
                                listener.onResponse(place);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                        listener.onResponse("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Utils", "Didn't work request queue");
                    }
                }
        );
        TripsterActivity.reqQ.add(stringRequest);
    }

    private String completeUri(String photoUri) {
        if (photoUri.contains("/")) {
            return photoUri;
        }
        return SERVER_URL + "/" + photoUri + ".jpg";
    }

    // Parcelling part
    private Event(Parcel in) {
        this.timeStamp = in.readLong();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.photoUris = Arrays.asList(in.readString().split(","));
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timeStamp);
        dest.writeDouble(lat);
        dest.writeDouble(lng);

        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (String uri : photoUris) {
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
