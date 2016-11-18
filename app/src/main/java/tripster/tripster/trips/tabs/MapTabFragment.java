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

import static tripster.tripster.TripsterActivity.MAPS_OPT;
import static tripster.tripster.TripsterActivity.MAPS_URL;

public class MapTabFragment extends Fragment implements OnMapReadyCallback {
  private static final String TAG = MapTabFragment.class.getName();

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
      Picasso.with(getContext()).load(new File(path)).resize(300, 300)
          .centerInside().into(onMap);
    } else {
      Log.d(TAG, "Positioning server photo on map:" + path);
      Picasso.with(getContext()).load(path).resize(300, 300)
          .centerInside().into(onMap);
    }
  }

  private void markPlaceName(final Event e) {
    e.onPlaceFound(new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        MarkerOptions options = new MarkerOptions().position(e.getLocation());
        if (!response.isEmpty()) {
          options.title(response);
        }
        mMap.addMarker(options);
      }
    });
  }
}