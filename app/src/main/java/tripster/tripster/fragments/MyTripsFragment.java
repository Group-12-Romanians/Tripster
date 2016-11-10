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

import tripster.tripster.R;
import tripster.tripster.adapters.TripPreviewAdapter;

public class MyTripsFragment extends Fragment {
  private static final String TAG = MyTripsFragment.class.getName();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_trips, container, false);
    GridView grid = (GridView) view.findViewById(R.id.myTrips);
    grid.setAdapter(new TripPreviewAdapter(getActivity()));
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Here I am");
        HomeFragment frag = new HomeFragment();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
      }
    });
    return view;
  }
}
