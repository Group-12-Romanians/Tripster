package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;

public class HomeFragment extends Fragment {

  private static final String SERVER_TRIP = TripsterActivity.SERVER_URL + "/trip";
  private static final String TAG = HomeFragment.class.getName();

  private FragmentTabHost mTabHost;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_home, container, false);
    String tripId = this.getArguments().getString("trip_id");
    mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
    mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

    if (tripId != null && tripId.equals("current")) {
      getCurrentTripInfo();
    } else {
      fetchTripInfo(tripId);
    }
    return rootView;
  }

  private void fetchTripInfo(String tripId) {
    String tripRequestUrl = SERVER_TRIP
        + "?trip_id="
        + tripId;
    StringRequest tripRequest = new StringRequest(Request.Method.GET,
        tripRequestUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String tripJSON) {
            Log.d(TAG, "Got following trip " + tripJSON);
            Bundle tripInfoBundle = new Bundle();
            tripInfoBundle.putString("trip_json", tripJSON);
            mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("Map"),
                PhotosOnMapFragment.class, tripInfoBundle);
            mTabHost.addTab(mTabHost.newTabSpec("photos").setIndicator("Photos"),
                PicturesFragment.class, tripInfoBundle);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get trip info: " + error.networkResponse.data);
          }
        });
    TripsterActivity.reqQ.add(tripRequest);
  }

  private void getCurrentTripInfo() {
    return currentTripInfo;
  }
}
