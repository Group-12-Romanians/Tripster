package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tripster.tripster.R;

public class TripsterFragment extends Fragment {

  private String TAG = TripsterFragment.class.getName();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_tripster, container, false);
//    Button start = (Button) view.findViewById(R.id.start_tracking);
//    start.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        if (!isServiceRunning(LocationService.class)) {
//          Intent serviceIntent = new Intent(getActivity(), LocationService.class);
//          serviceIntent.putExtra("flag", "start");
//          getActivity().startService(serviceIntent);
//        } else {
//          Toast.makeText(getActivity(), "Already recording another trip.", Toast.LENGTH_LONG).show();
//        }
//      }
//    });
//
//    Button stop = (Button) view.findViewById(R.id.stop_tracking);
//    stop.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        if (isServiceRunning(LocationService.class)) {
//          Intent serviceIntent = new Intent(getActivity(), LocationService.class);
//          serviceIntent.putExtra("flag", "stop");
//          getActivity().startService(serviceIntent);
//        } else {
//          Toast.makeText(getActivity(), "There is no trip to stop.", Toast.LENGTH_LONG).show();
//        }
//      }
//    });

    return view;
  }
}
