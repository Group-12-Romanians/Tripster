package tripster.tripster.UILayer.newsfeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tripster.tripster.R;

public class NewsfeedFragment extends Fragment {
  private static final String TAG = NewsfeedFragment.class.getName();

  private NewsfeedAdapter newsfeedAdapter;
  private ListView newsfeed;

  private Set<Object> friends = new HashSet<>();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
    newsfeed = (ListView) view.findViewById(R.id.userStories);

    return view;
  }

  private void initItemListAdapter() {
    List<Pair<Document, Document>> userStories = new ArrayList<>();

    final Database db;
    final Query friendsQuery = db.getView("friendsView").createQuery().setKeys(Collections.singletonList(getCurrentUserId()));
    LiveQuery fLiveQuery = friendsQuery.toLiveQuery();
    fLiveQuery.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<Document> friendDocs = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < event.getRows().getCount(); i++) {
          String friendId = event.getRows().getRow(i).getDocumentId();
          if (!friends.contains(friendId)) {
            friends.add(friendId);
            changed = true;
          }
        }
        if (changed) {
          List<Object> keys = new ArrayList<>(friends);
          Query friendsTripsQuery = db.getView("tripsView").createQuery();
          friendsTripsQuery.setKeys(keys);

          LiveQuery lQ_ = friendsTripsQuery.toLiveQuery();
          lQ_.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {

            }
          });
        }
      }
    });


    newsfeedAdapter = new NewsfeedAdapter(
        getActivity(),
        R.layout.user_story,
        R.id.tripDescription,
        userStories);
    newsfeed.setAdapter(newsfeedAdapter);
  }
}