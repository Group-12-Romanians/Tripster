package tripster.tripster.trips.tabs;

import android.content.SharedPreferences;
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

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;
import tripster.tripster.trips.tabs.timeline.TimelineFragment;

import static android.content.Context.MODE_PRIVATE;
import static tripster.tripster.TripsterActivity.LOCATIONS_FILE_PATH;
import static tripster.tripster.TripsterActivity.SHARED_PREF_PHOTOS;

public class HomeFragment extends Fragment {

  private static final String SERVER_TRIP = TripsterActivity.SERVER_URL + "/get_trip";
  private static final String TAG = HomeFragment.class.getName();

  private FragmentTabHost mTabHost;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_home, container, false);
    String tripId = this.getArguments().getString("trip_id");
    mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
    mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

    if (tripId != null && tripId.equals("current")) { // If local trip
      initTabs(getLocalEventsList());
    } else {
      fetchAndInitTrip(tripId);
    }
    return rootView;
  }

  private ArrayList<Event> getLocalEventsList() {
    ArrayList<Event> events = new ArrayList<>();
    try {
        File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
        FileInputStream locationStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
        String line = reader.readLine();

        String[] tripInfo = line.split(","); //TODO: might be needed for more details

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(SHARED_PREF_PHOTOS, MODE_PRIVATE);
        while ((line = reader.readLine()) != null) {
          Log.d(TAG, "Read line: " + line);
          String[] eventInfo = line.split(",");

          long timeStamp = Long.parseLong(eventInfo[0]);
          double lat = Double.parseDouble(eventInfo[1]);
          double lng = Double.parseDouble(eventInfo[2]);

          Event event = new Event(timeStamp, lat, lng);

          for(int i = 3; i < eventInfo.length; i++) {
            event.addPhotoUri(sharedPrefs.getString(eventInfo[i], ""));
          }

          events.add(event);
        }
      } catch (FileNotFoundException e) {
        Log.d(TAG, "No file to read events from.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    return events;
  }

  private void fetchAndInitTrip(String tripId) {
    String tripRequestUrl = SERVER_TRIP
        + "?trip_id="
        + tripId;
    StringRequest tripRequest = new StringRequest(Request.Method.GET,
        tripRequestUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String tripStr) {
            Log.d(TAG, tripStr);
            ArrayList<Event> events = parseJSONTrip(tripStr);
            initTabs(events);
          }

          private ArrayList<Event> parseJSONTrip(String tripStr) {
            ArrayList<Event> events = new ArrayList<>();
            try {
              JSONObject tripJSON = new JSONObject(tripStr);
              JSONArray eventsJSON = tripJSON.getJSONArray("events");
              for (int i = 0; i < eventsJSON.length(); i++) {
                JSONObject eventJSON = eventsJSON.getJSONObject(i);

                JSONArray imgsJSON = eventJSON.getJSONArray("img_ids");

                long timeStamp = Long.parseLong(eventJSON.getString("time"));
                double lat = Double.parseDouble(eventJSON.getString("lat"));
                double lng = Double.parseDouble(eventJSON.getString("lng"));
                Event event = new Event(timeStamp, lat, lng);
                for (int j = 0; j < imgsJSON.length(); j++) {
                  event.addPhotoUri(imgsJSON.getString(j));
                }
                events.add(event);
              }
            } catch (JSONException e) {
              Log.e(TAG, e.toString());
            }
            return events;
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

  private void initTabs(ArrayList<Event> events) {
    Bundle tripInfoBundle = new Bundle();
    tripInfoBundle.putParcelableArrayList("events", events);

    mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("Map"), MapTabFragment.class, tripInfoBundle);
    mTabHost.addTab(mTabHost.newTabSpec("photos").setIndicator("Photos"), TimelineFragment.class, tripInfoBundle);
  }
}
