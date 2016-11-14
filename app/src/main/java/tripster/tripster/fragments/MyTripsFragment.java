package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.Trip;
import tripster.tripster.TripsterActivity;
import tripster.tripster.adapters.TripPreviewAdapter;

import static tripster.tripster.TripsterActivity.LOCATIONS_FILE_PATH;
import static tripster.tripster.TripsterActivity.SERVER_URL;

public class MyTripsFragment extends Fragment {
  private static final String TAG = MyTripsFragment.class.getName();
  private static final String SERVER_TRIPS = SERVER_URL + "/get_trips";
  public static final String CURRENT_TRIP_IMAGE_URL
      = "https://cdn1.tekrevue.com/wp-content/uploads/2015/04/map-location-pin.jpg";
  private GridView grid;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_trips, container, false);
    grid = (GridView) view.findViewById(R.id.myTrips);
    fetchAndSetUserTrips();
    return view;
  }

  private void fetchAndSetUserTrips() {
    String tripsRequestUrl = SERVER_TRIPS
        + "?user_id="
        + TripsterActivity.USER_ID;
    StringRequest tripsRequest = new StringRequest(Request.Method.GET,
        tripsRequestUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String tripsJSON) {
            Log.d(TAG, "Got following trips " + tripsJSON);
            List<Trip> myTrips = parseJSONResponse(tripsJSON);
            populateGrid(myTrips);
          }

          private List<Trip> parseJSONResponse(String tripsJSON) {
            // TODO: Implement this once server done.
            return new LinkedList<>();
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get the list of trips: " + error.networkResponse.data);
          }
        });
    TripsterActivity.reqQ.add(tripsRequest);
  }

  private void populateGrid(List<Trip> myTrips) {
    Trip currentTrip = getCurrentTrip();
    if (currentTrip != null) {
      myTrips.add(0, currentTrip);
    }
    grid.setAdapter(new TripPreviewAdapter(getActivity(), myTrips));
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "A trip has been chosen!");
        accessTrip((Trip) view.getTag());
      }
    });
  }

  private void accessTrip(Trip trip) {
    HomeFragment frag = new HomeFragment();
    Bundle arguments = new Bundle();
    arguments.putString("trip_id", trip.getId());
    frag.setArguments(arguments);
    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
  }

  private Trip getCurrentTrip() {
    try {
      File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String[] tripInfo = reader.readLine().split(",");
      String tripId = "current";
      String tripName = tripInfo[1];
      return new Trip(tripId, tripName, CURRENT_TRIP_IMAGE_URL);
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No current trip");
      return null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
