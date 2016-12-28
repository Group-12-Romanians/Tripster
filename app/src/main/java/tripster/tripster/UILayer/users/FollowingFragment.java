package tripster.tripster.UILayer.users;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tripster.tripster.UILayer.search.SearchableUsersFragment;

import static tripster.tripster.Constants.FOLLOWING_BY_USER;
import static tripster.tripster.Constants.USER_ID;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class FollowingFragment extends SearchableUsersFragment {
  @Override
  public Query getQuery() {
    String userId = getArguments().getString(USER_ID);
    Query q = tDb.getDb().getExistingView(FOLLOWING_BY_USER).createQuery();
    q.setKeys(Collections.<Object>singletonList(userId));
    q.setMapOnly(true);
    return q;
  }

  @Override
  public List<String> getResults(LiveQuery.ChangeEvent event) {
    List<String> results = new ArrayList<>();
    for (int i = 0; i < event.getRows().getCount(); i++) {
      QueryRow r = event.getRows().getRow(i);
      results.add(r.getDocumentId().split(":")[1]); // second part of docId is the subject of the follow
    }
    return results;
  }
}
