package tripster.tripster.UILayer.users;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static tripster.tripster.Constants.FRIENDS_BY_USER;
import static tripster.tripster.Constants.USERS_BY_ID;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class SearchForUsersFragment extends Fragment {
  private static final String TAG = SearchForUsersFragment.class.getName();

  private SearchableAdapter searchableAdapter;
  private String myId;
  private LiveQuery usersLQ;

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
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (myId.equals("none")) {
      restartAllUsersLiveQuery();
    } else {
      restartUsersLiveQuery();
    }
  }

  private void restartAllUsersLiveQuery() {
    assertSame(myId, "none");
    Query q = tDb.getDb().getExistingView(USERS_BY_ID).createQuery();
    usersLQ = q.toLiveQuery();
    usersLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          results.add(r.getDocumentId());
        }
        Collections.sort(results, new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        });
        initSearchableAdapter(results);
      }
    });
    usersLQ.start();
  }

  private void restartUsersLiveQuery() {
    assertNotSame(myId, "none");
    Query q = tDb.getDb().getExistingView(FRIENDS_BY_USER).createQuery();
    q.setKeys(Collections.singletonList((Object) myId));
    usersLQ = q.toLiveQuery();
    usersLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          results.add(r.getDocumentId());
        }
        Collections.sort(results, new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        });
        initSearchableAdapter(results);
      }
    });
    usersLQ.start();
  }

  @Override
  public void onPause() {
    usersLQ.stop();
    super.onPause();
  }

  private void initSearchableAdapter(List<String> users) {
    searchableAdapter = new SearchableAdapter(
        getActivity(),
        users);
    assertNotNull(getView());
    ((ListView) getView().findViewById(R.id.friends_list)).setAdapter(searchableAdapter);
  }
}
