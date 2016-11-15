package tripster.tripster.trips.tabs;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;

public class MapTabFragment extends Fragment implements OnMapReadyCallback {
  private static final String TAG = MapTabFragment.class.getName();
  private static final String MAPS_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch";
  private static final String MAPS_OPT = "&radius=50&key=AIzaSyBEcADKicF0ZeIooOSbh12Vu7BVyDOIjik";

  private GoogleMap mMap;

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
      Log.d(TAG, "Map is saved from memory.");
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
    ArrayList<Event> events = this.getArguments().getParcelableArrayList("events");
    if (events == null) {
      Log.e(TAG, "Passed bundle to tab incorrectly");
      return;
    }
    for(Event e : events) {
      Log.d(TAG, "Processing event: " + e.toString());
      if(e.getPhotoUris().size() == 0) {
        markPlaceName(e);
      } else {
        markPhoto(e);
      }
    }
  }

  private void markPhoto(final Event e) {
    Target onMap = new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mMap.addMarker(new MarkerOptions().position(e.getLocation()).
            icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        Log.d(TAG, "added photo marker for photo: " + e.getPhotoUris().toString());
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {
        Log.d(TAG, "Failed to load picture with Picasso");
      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {

      }
    };
    String path = e.getPhotoUris().get(0);

    if (path.charAt(0) == '/') { // is photo from local storage
      Log.d(TAG, "Positioning local photo on map:" + path);
      Picasso.with(getContext()).load(new File(path)).resize(300, 300).into(onMap);
    } else {
      Log.d(TAG, "Positioning server photo on map:" + path);
      Picasso.with(getContext()).load(path).resize(300, 300).into(onMap);
    }
  }

  private void markPlaceName(final Event e) {
    final LatLng location = e.getLocation();
    String url = MAPS_URL + "/json?location=" + location.latitude + "," + location.longitude + MAPS_OPT;
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
      new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          try {
            MarkerOptions options = new MarkerOptions().position(location);

            Log.d(TAG, "Place name response is: " + response);

            JSONObject respJSON = new JSONObject(response);
            JSONArray resultsJSON = respJSON.getJSONArray("results");
            if (resultsJSON.length() > 0) {
              String placeName = resultsJSON.getJSONObject(0).getString("name");
              options.title(placeName);
            }

            mMap.addMarker(options);
          } catch (JSONException e) {
            Log.e(TAG, e.toString());
          }
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
}