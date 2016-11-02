package tripster.tripster.fragments;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import java.util.List;
import java.util.Scanner;

import tripster.tripster.R;
import tripster.tripster.pictures.Picture;

public class PhotosOnMapFragment extends Fragment implements OnMapReadyCallback {
  private static final String TAG = PhotosOnMapFragment.class.getName();
  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private GoogleMap mMap;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_map, container, false);
    AssetManager am = getActivity().getAssets();
    try {
      parseLocationFile(new Scanner(am.open("Locations")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    mMap.clear();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    List<LatLng> locations = getLocationsFromFile();
    for (LatLng location : locations) {
      mMap.addMarker(new MarkerOptions().position(location).title("Marker"));
    }

    if (PicturesFragment.pictures != null) {
      for (Picture picture : PicturesFragment.pictures) {
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
            icon(BitmapDescriptorFactory.fromBitmap(picture.getBitmap(200))));
      }
    }
  }

  public void parseLocationFile(Scanner scanner) {
    String requestHead = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
        "location=";
    String requestTail = "&radius=50&key=AIzaSyBEcADKicF0ZeIooOSbh12Vu7BVyDOIjik";

    while (scanner.hasNextLine()) {
      String latLongString = scanner.nextLine();
      Double latitude = Double.parseDouble(latLongString.split(",")[0]);
      Double longitude = Double.parseDouble(latLongString.split(",")[1]);
      final LatLng latLng = new LatLng(latitude, longitude);
      String url = requestHead + latLongString + requestTail;

      RequestQueue queue = Volley.newRequestQueue(getActivity());
      StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
          new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
              try {
                Log.d("RESPONSE request place", response);
                JSONObject clientObject = new JSONObject(response);
                JSONArray array = clientObject.getJSONArray("results");
                int position = array.length() == 1 ? 0 : 1;
                String placeName = array.getJSONObject(position).getString("name");
                mMap.addMarker(new MarkerOptions()
                    .position(latLng).title(placeName));
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

  private List<LatLng> getLocationsFromFile() {
    List<LatLng> locations = new ArrayList<>();
    try {
      File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] locationInfo = line.split(",");
        double latitude = Double.parseDouble(locationInfo[1]);
        double longitude = Double.parseDouble(locationInfo[2]);
        locations.add(new LatLng(latitude, longitude));
      }
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return locations;
  }

  private boolean isLocationNull(Picture picture) {
    return picture.getLatitude() == null ||
        picture.getLongitude() == null;
  }
}