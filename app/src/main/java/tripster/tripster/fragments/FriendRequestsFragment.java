package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.adapters.NotificationsListAdapter;

public class FriendRequestsFragment extends Fragment {

  List<String> friendsNames;
  ListView pendingRequestsListView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_requests, container, false);
    pendingRequestsListView = (ListView) view.findViewById(R.id.notifications_list);
    getFriendsNames();
    NotificationsListAdapter notificationsAdapter =
        new NotificationsListAdapter(getActivity(), friendsNames);
    pendingRequestsListView.setAdapter(notificationsAdapter);
    return view;
  }

  public void getFriendsNames() {
    //TODO: send request to get friends;
    friendsNames = new ArrayList<>();
    friendsNames.add("User1");
    friendsNames.add("User2");
    friendsNames.add("User3");
  }
}
