package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tripster.tripster.R;

public class HomeFragment extends Fragment {

  private FragmentTabHost mTabHost;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_home, container, false);


    mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
    mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

    mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("Map"),
        PhotosOnMapFragment.class, null);
    mTabHost.addTab(mTabHost.newTabSpec("photos").setIndicator("Photos"),
        PicturesFragment.class, null);
    return rootView;
  }
}
