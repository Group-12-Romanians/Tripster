package tripster.tripster.friends;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.User;

public class FriendsFragment extends Fragment {

  private FragmentTabHost mTabHost;
  public static List<User> friends;
  public static List<Pair<String, String>> friendRequests;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_home, container, false);
    friends = new ArrayList<>();
    friendRequests = new ArrayList<>();

    mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
    mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

    mTabHost.addTab(mTabHost.newTabSpec("search").setIndicator("Search"),
        SearchForUsersFragment.class, null);
    mTabHost.addTab(mTabHost.newTabSpec("myFriends").setIndicator("MyFriends"),
        MyFriendsFragment.class, null);
    mTabHost.addTab(mTabHost.newTabSpec("requests").setIndicator("Requests"),
        FriendRequestsFragment.class, null);
    return rootView;
  }
}
