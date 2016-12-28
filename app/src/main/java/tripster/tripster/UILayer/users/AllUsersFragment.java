package tripster.tripster.UILayer.users;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.UILayer.search.SearchableUsersFragment;

import static tripster.tripster.Constants.USERS_BY_ID;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class AllUsersFragment extends SearchableUsersFragment {

  @Override
  public Query getQuery() {
    return tDb.getDb().getExistingView(USERS_BY_ID).createQuery();
  }

  @Override
  public List<String> getResults(LiveQuery.ChangeEvent event) {
    List<String> results = new ArrayList<>();
    for (int i = 0; i < event.getRows().getCount(); i++) {
      QueryRow r = event.getRows().getRow(i);
      results.add(r.getDocumentId());
    }
    return results;
  }
}
