package tripster.tripster.UILayer.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tripster.tripster.R;

public abstract class SearchableUsersFragment extends Fragment {
  private static final String TAG = SearchableUsersFragment.class.getName();

  private SearchableAdapter searchableAdapter;
  private ListView usersList;

  private LiveQuery lQ;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_friends, container, false);
    usersList = (ListView) view.findViewById(R.id.friends_list);
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
    restartLQ();
  }

  // should return query for this list of searchable users
  public abstract Query getQuery();

  // return list of users to show from the event (which is produced with the query given)
  public abstract List<String> getResults(LiveQuery.ChangeEvent event);

  private void restartLQ() {
    Query q = getQuery();
    lQ = q.toLiveQuery();
    lQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<String> results = getResults(event);
        Collections.sort(results, new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            initSearchableAdapter(results);
          }
        });
      }
    });
    lQ.start();
  }

  @Override
  public void onPause () {
    try {
      lQ.stop();
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }

  void initSearchableAdapter(final List<String> users) {
    searchableAdapter = new SearchableAdapter(
        getActivity(),
        users);
    usersList.setAdapter(searchableAdapter);
  }
}
