package tripster.tripster.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tripster.tripster.R;

public class MyFriendsFragment extends Fragment {

  ListView friendsListView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.my_friends_fragment, container, false);
    friendsListView = (ListView) view.findViewById(R.id.my_friends_list);
    FriendsListAdapter friendsListAdapter
        = new FriendsListAdapter(getActivity(), FriendsFragment.friends);
    friendsListView.setAdapter(friendsListAdapter);
    return view;
  }
}
