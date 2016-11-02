package tripster.tripster.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tripster.tripster.R;
import tripster.tripster.services.LocationService;

public class TripsterFragment extends Fragment {

  private String TAG = TripsterFragment.class.getName();
  private Intent serviceIntent;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_tripster, container, false);
    Button button = (Button) view.findViewById(R.id.start_tracking);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        serviceIntent = new Intent(getActivity(), LocationService.class);
        if (!isServiceRunning(LocationService.class)) {
          getActivity().startService(serviceIntent);
        }
      }
    });
    return view;
  }

  private boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        Log.i("isMyServiceRunning?", true + "");
        return true;
      }
    }
    Log.i("isMyServiceRunning?", false + "");
    return false;
  }
}
