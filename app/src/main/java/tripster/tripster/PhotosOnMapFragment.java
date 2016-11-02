package tripster.tripster;

import android.content.res.AssetManager;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class PhotosOnMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setContentView(R.layout.activity_maps);
        AssetManager am = getContext().getAssets();
        try {
            parseLocationFile(new Scanner(am.open("Locations")));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}