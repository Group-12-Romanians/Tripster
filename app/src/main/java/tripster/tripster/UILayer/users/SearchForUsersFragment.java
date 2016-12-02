package tripster.tripster.UILayer.users;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tripster.tripster.R;
import tripster.tripster.dataLayer.events.FriendsChangedEvent;
import tripster.tripster.dataLayer.events.UsersChangedEvent;

public class SearchForUsersFragment extends Fragment {
  private static final String TAG = SearchForUsersFragment.class.getName();

  private SearchableAdapter searchableAdapter;
  private ListView friendsList;
  private Map<String, Document> users;
  private Set<String> myFriends;
  private String myId;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_friends, container, false);
    myId = getArguments().getString("userId"); // We will only see the friends of this id
    // if this is "none" then we will see all the users

    EditText searchBar = (EditText) view.findViewById(R.id.search);
    searchBar.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        System.out.println("Text [" + s + "]");
        if (searchableAdapter != null) {
          searchableAdapter.getFilter().filter(s.toString());
        }

      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    friendsList = (ListView) view.findViewById(R.id.friends_list);
    return view;
  }

  @Override
  public void onResume() {
    Log.d(TAG, "Register fragment");
    users = new HashMap<>();
    myFriends = new HashSet<>();
    EventBus.getDefault().register(this);
    super.onResume();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onUsersChangedEvent(UsersChangedEvent event) {
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    for (int i = 0; i < changes.getCount(); i++) {
      Document doc = changes.getRow(i).getDocument();
      users.put(doc.getId(), doc);
    }
    initSearchableAdapter();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onFriendsChangedEvent(FriendsChangedEvent event) {
    if (!myId.equals("none")) {
      LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
      QueryEnumerator changes = liveChangeEvent.getRows();
      for (int i = 0; i < changes.getCount(); i++) {
        if (changes.getRow(i).getKey().equals("confirmed")) {
          Document doc = changes.getRow(i).getDocument();
          if (doc.getProperty("sender").equals(myId)) {
            myFriends.add((String) doc.getProperty("receiver"));
          } else if (doc.getProperty("receiver").equals(myId)) {
            myFriends.add((String) doc.getProperty("sender"));
          }
        }
      }
      //TODO:add reinit
      initSearchableAdapter();
    }
  }

  private void initSearchableAdapter() {
    List<Document> list;
    if (!myId.equals("none")) {
      list  = new ArrayList<>();
      for (String friend : myFriends) {
        list.add(users.get(friend));
      }
    } else {
      list = new ArrayList<>(users.values());
    }
    searchableAdapter = new SearchableAdapter(
        getActivity(),
        list);
    friendsList.setAdapter(searchableAdapter);
    friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "I clicked on a user, I want to see him :)");
        SearchableAdapter.ViewHolder holder = (SearchableAdapter.ViewHolder) view.getTag();
        accessUserProfile(holder.doc);
      }
    });
  }

  private void accessUserProfile(Document doc) {
    // Change to the corresponding UserProfile.
    UserProfileFragment frag = new UserProfileFragment();
    Bundle arguments = new Bundle();
    arguments.putString("userId", doc.getId());
    frag.setArguments(arguments);
    FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "UnRegister fragment");
    EventBus.getDefault().unregister(this);
    super.onPause();
  }
}
