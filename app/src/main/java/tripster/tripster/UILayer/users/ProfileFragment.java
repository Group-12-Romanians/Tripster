package tripster.tripster.UILayer.users;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.FOLLOWERS_BY_USER;
import static tripster.tripster.Constants.FOLLOWING_BY_USER;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.TRIP_STOPPED_AT_K;
import static tripster.tripster.Constants.USER_ABOUT_K;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_ID;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public abstract class ProfileFragment extends Fragment {
  private static final String TAG = ProfileFragment.class.getName();

  private TransactionManager tM;
  protected String userId;

  private LiveQuery tripsLQ;

  private LiveQuery followersNoLQ;
  private LiveQuery followingNoLQ;

  private TextView name;
  private TextView about;
  private ImageView avatar;
  private Button noOfFollowersButton;
  private Button noOfFollowingButton;
  private Button noOfTripsButton;
  private GridView grid;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_trips, container, false);
    userId = getArguments().getString(USER_ID);
    tM = new TransactionManager(getContext());

    name = (TextView) view.findViewById(R.id.userName);
    about = (TextView) view.findViewById(R.id.userAbout);
    avatar = (ImageView) view.findViewById(R.id.avatar);
    noOfFollowersButton = (Button) view.findViewById(R.id.noOfFollowers);
    noOfFollowersButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tM.accessFollowersOfUser(userId);
      }
    });
    noOfFollowingButton = (Button) view.findViewById(R.id.noOfFollowing);
    noOfFollowingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tM.accessFollowingOfUser(userId);
      }
    });
    noOfTripsButton = (Button) view.findViewById(R.id.noOfTrips);
    grid = (GridView) view.findViewById(R.id.myTrips);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    restartUserListener();
    restartTripsLiveQuery();
    restartFollowersNoLiveQuery();
    restartFollowingNoLiveQuery();
  }

  private void restartUserListener() {
    updateUserDetails();
    tDb.getDocumentById(userId).addChangeListener(userChangeListener);
  }

  private void restartFollowingNoLiveQuery() {
    Query q = tDb.getDb().getExistingView(FOLLOWING_BY_USER).createQuery();
    q.setKeys(Collections.<Object>singletonList(userId));
    followingNoLQ = q.toLiveQuery();
    followingNoLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(final LiveQuery.ChangeEvent event) {
        if (event.getRows().getCount() == 1) {
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              noOfFollowingButton.setText(String.valueOf(event.getRows().getRow(0).getValue()));
            }
          });
        } else {
          Log.e(TAG, "No realtions or some problem: " + event.getRows().getCount());
        }
      }
    });
    followingNoLQ.start();
  }

  private void restartFollowersNoLiveQuery() {
    Query q = tDb.getDb().getExistingView(FOLLOWERS_BY_USER).createQuery();
    q.setKeys(Collections.<Object>singletonList(userId));
    followersNoLQ = q.toLiveQuery();
    followersNoLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(final LiveQuery.ChangeEvent event) {
        if (event.getRows().getCount() == 1) {
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              noOfFollowersButton.setText(String.valueOf(event.getRows().getRow(0).getValue()));
            }
          });
        } else {
          Log.e(TAG, "No realtions or some problem: " + event.getRows().getCount());
        }
      }
    });
    followersNoLQ.start();
  }

  public void restartTripsLiveQuery() {
    Query q = tDb.getDb().getExistingView(TRIPS_BY_OWNER).createQuery();
    q.setKeys(Collections.singletonList((Object) userId));
    tripsLQ = q.toLiveQuery();
    tripsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<Pair<Long, String>> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          if ((int) r.getValue() <= getLevel()) {
            Document d = r.getDocument();
            Long stoppedAt = (Long) d.getProperty(TRIP_STOPPED_AT_K);
            if (stoppedAt != null) {
              results.add(new Pair<>(stoppedAt, d.getId()));
            } else {
              results.add(new Pair<>(Long.MAX_VALUE, d.getId()));
            }
          }
        }
        Collections.sort(results, new Comparator<Pair<Long, String>>() {
          @Override
          public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
            return o2.first.compareTo(o1.first);
          }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            noOfTripsButton.setText(String.valueOf(results.size()));
            initItemGridAdapter(results);
          }
        });
      }
    });
    tripsLQ.start();
  }

  protected abstract int getLevel();

  private Document.ChangeListener userChangeListener = new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      updateUserDetails();
    }
  };

  public void updateUserDetails() {
    Document userDoc = tDb.getDocumentById(userId);
    name.setText((String) userDoc.getProperty(USER_NAME_K));
    String aboutUser = (String) userDoc.getProperty(USER_ABOUT_K);
    if (aboutUser != null) {
      about.setText(aboutUser);
    } else {
      about.setVisibility(View.GONE);
    }
    new Image((String) userDoc.getProperty(USER_AVATAR_K)).displayIn(avatar);
  }

  @Override
  public void onPause() {
    try {
      tDb.getDocumentById(userId).removeChangeListener(userChangeListener);
      tripsLQ.stop();
      followersNoLQ.stop();
      followingNoLQ.stop();
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }

  private void initItemGridAdapter(List<Pair<Long, String>> results) {
    List<String> trips = new ArrayList<>();
    for (Pair<Long, String> p : results) {
      trips.add(p.second);
    }
    UserTripsAdapter tripsAdapter = new UserTripsAdapter(
        getContext(),
        R.layout.trips_grid_item,
        R.id.tripName,
        trips);
    grid.setAdapter(tripsAdapter);
  }
}

