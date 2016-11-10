package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;
import tripster.tripster.pictures.Picture;

public class PhotosOnMapFragment extends Fragment implements OnMapReadyCallback {
  private static final String TAG = PhotosOnMapFragment.class.getName();
  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private GoogleMap mMap;
  private HashMap<Long, LatLng> locationHistory;

  private static View view;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (view != null) {
      ViewGroup parent = (ViewGroup) view.getParent();
      if (parent != null) {
        parent.removeView(view);
      }
    }

    try {
      view = inflater.inflate(R.layout.fragment_map, container, false);
    } catch (InflateException e) {
      /* map is already there, just return view as it is */
      Log.d(TAG, "locations on map exception");
    }
    addPlacesOnMap();
    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    return view;
  }

  private void addPlacesOnMap() {
    locationHistory = getLocationHistoryFromFile();
    List<LatLng> locations = new ArrayList<>();
    for (Map.Entry<Long, LatLng> location : locationHistory.entrySet()) {
      locations.add(location.getValue());
    }
    getPlacesNames(locations);
  }

  @Override
  public void onPause() {
    super.onPause();
    mMap.clear();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    if (HomeFragment.pictures != null) {
      for (Picture picture : HomeFragment.pictures) {
        LatLng location;
        if (isLocationNull(picture)) {
          long pictureDate = picture.getDateTaken();
          location = new LatLng(51.3030, 0.0732);
        } else {
          double latitude = Double.parseDouble(picture.getLatitude());
          double longitude = Double.parseDouble(picture.getLongitude());
          location = new LatLng(latitude, longitude);
        }
        mMap.addMarker(new MarkerOptions().position(location).
            icon(BitmapDescriptorFactory.fromBitmap(picture.getBitmap(100))));
      }
    }
  }

  public void getPlacesNames(List<LatLng> locations) {
    String requestHead = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
        "location=";
    String requestTail = "&radius=50&key=AIzaSyBEcADKicF0ZeIooOSbh12Vu7BVyDOIjik";

   for (final LatLng location : locations) {
      String url = requestHead + location.latitude + "," + location.longitude + requestTail;

      RequestQueue queue = Volley.newRequestQueue(getActivity());
      StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
          new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
              try {
                Log.d("RESPONSE request place", response);
                JSONObject clientObject = new JSONObject(response);
                JSONArray array = clientObject.getJSONArray("results");
                MarkerOptions options = new MarkerOptions().position(location);
                if (array.length() > 0) {
                  int position = array.length() == 1 ? 0 : 1;
                  String placeName = array.getJSONObject(position).getString("name");
                  options.title(placeName);
                }
                if (mMap != null) {
                  mMap.addMarker(options);
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
          },
          new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              Log.d("Utils", "Didn't work request queue");
            }
          });

      queue.add(stringRequest);
    }

  }

  private HashMap<Long, LatLng> getLocationHistoryFromFile() {
    HashMap<Long, LatLng> locationHistory = new HashMap<>();
    try {
      File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] locationInfo = line.split(",");
        long time = Long.parseLong(locationInfo[0]);
        double latitude = Double.parseDouble(locationInfo[1]);
        double longitude = Double.parseDouble(locationInfo[2]);
        locationHistory.put(time, new LatLng(latitude, longitude));
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return locationHistory;
  }

  private boolean isLocationNull(Picture picture) {
    return picture.getLatitude() == null ||
        picture.getLongitude() == null;
  }
}