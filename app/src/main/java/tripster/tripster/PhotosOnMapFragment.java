package tripster.tripster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PhotosOnMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setContentView(R.layout.activity_maps);
        parseLocationFile(null);
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //TODO: Remove this. Added for guidance and testing.
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        LatLng london = new LatLng(47, 0.132);
        LatLng london1 = new LatLng(49, -0.132);
        LatLng london2 = new LatLng(48.3, 0.532);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.addMarker(new MarkerOptions().position(london).title("London Baby"));
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(london).
                icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        mMap.addMarker(new MarkerOptions().position(london1).
                icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        mMap.addMarker(new MarkerOptions().position(london2).
                icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        // newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.picture));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void parseLocationFile(File file) {
        final LatLng newLocation = new LatLng(51.507653, -0.165726);
        String url2 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
             "location=51.507653,-0.165726&radius=100&key=AIzaSyBEcADKicF0ZeIooOSbh12Vu7BVyDOIjik";

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("RESPONSE request place", response);
                            JSONObject clientObject = new JSONObject(response);
                            JSONArray array = clientObject.getJSONArray("results");
                            String placeName = array.getJSONObject(1).getString("name");
                            mMap.addMarker(new MarkerOptions().position(newLocation).title(placeName));
                            Log.d("Place Name",placeName);
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