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
import tripster.tripster.adapters.FriendsListAdapter;

public class MyFriendsFragment extends Fragment {

  List<String> friendsNames;
  ListView friendsListView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.my_friends_fragment, container, false);
    friendsListView = (ListView) view.findViewById(R.id.my_friends_list);
    getFriendsNames();
    FriendsListAdapter friendsListAdapter
        = new FriendsListAdapter(getActivity(), friendsNames);
    friendsListView.setAdapter(friendsListAdapter);
    return view;
  }

  public void getFriendsNames() {
    //TODO: send request to get friends;
    friendsNames = new ArrayList<>();
    friendsNames.add("Ana");
    friendsNames.add("Mama");
  }
}
