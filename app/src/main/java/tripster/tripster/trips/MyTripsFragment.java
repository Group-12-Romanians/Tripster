package tripster.tripster.trips;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;
import tripster.tripster.trips.tabs.HomeFragment;

import static tripster.tripster.TripsterActivity.LOCATIONS_FILE_PATH;
import static tripster.tripster.TripsterActivity.SERVER_URL;

public class MyTripsFragment extends Fragment {
  private static final String TAG = MyTripsFragment.class.getName();
  private static final String SERVER_TRIPS = SERVER_URL + "/my_trips";
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
    StringRequest tripsRequest = new StringRequest(Request.Method.GET, tripsRequestUrl, new Response.Listener<String>() {
      @Override
      public void onResponse(String tripsStr) {
        Log.d(TAG, "Got following trips " + tripsStr);
        List<TripPreview> myTrips = parseJSONResponse(tripsStr);
        populateGrid(myTrips);
      }

      private List<TripPreview> parseJSONResponse(String tripsStr) {
        List<TripPreview> trips = new LinkedList<>();
        try {
          JSONArray tripsJSON = new JSONArray(tripsStr);
          for (int i = 0; i < tripsJSON.length(); i++) {
            JSONObject tripJSON = tripsJSON.getJSONObject(i);
            String tripName = tripJSON.getString("name");
            String tripId = tripJSON.getString("trip_id");
            String tripPreview = SERVER_URL + "/" + tripJSON.getString("preview") + ".jpg";
            trips.add(new TripPreview(tripId, tripName, tripPreview));
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
        return trips;
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Unable to get the list of trips");
      }
    });
    TripsterActivity.reqQ.add(tripsRequest);
  }

  private void populateGrid(List<TripPreview> myTrips) {
    TripPreview currentTrip = getCurrentTrip();
    if (currentTrip != null) {
      myTrips.add(0, currentTrip);
    }
    grid.setAdapter(new TripPreviewAdapter(getActivity(), myTrips));
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "A trip has been chosen!");
        accessTrip((TripPreview) view.getTag());
      }
    });
  }

  private void accessTrip(TripPreview trip) {
    HomeFragment frag = new HomeFragment();
    Bundle arguments = new Bundle();
    arguments.putString("trip_id", trip.getId());
    frag.setArguments(arguments);
    FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }

  private TripPreview getCurrentTrip() {
    try {
      File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line = reader.readLine();
      Log.d(TAG, line);
      String[] tripInfo = line.split(",");
      String tripId = "current";
      String tripName = tripInfo[1];
      return new TripPreview(tripId, tripName, CURRENT_TRIP_IMAGE_URL);
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No current trip");
      return null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
