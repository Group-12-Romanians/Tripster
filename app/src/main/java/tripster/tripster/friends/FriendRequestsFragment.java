package tripster.tripster.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tripster.tripster.R;

public class FriendRequestsFragment extends Fragment {
  ListView pendingRequestsListView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_requests, container, false);
    pendingRequestsListView = (ListView) view.findViewById(R.id.notifications_list);
    NotificationsListAdapter notificationsAdapter =
        new NotificationsListAdapter(getActivity(), FriendsFragment.friendRequests);
    pendingRequestsListView.setAdapter(notificationsAdapter);
    return view;
  }
}
