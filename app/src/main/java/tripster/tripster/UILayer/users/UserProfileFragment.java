package tripster.tripster.UILayer.users;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.FRIENDS_BY_USER;
import static tripster.tripster.Constants.FS_LEVEL_CONFIRMED;
import static tripster.tripster.Constants.FS_LEVEL_K;
import static tripster.tripster.Constants.FS_LEVEL_SENT;
import static tripster.tripster.Constants.FS_RECEIVER_K;
import static tripster.tripster.Constants.FS_SENDER_K;
import static tripster.tripster.Constants.FS_TIME_K;
import static tripster.tripster.Constants.TRIPS_BY_OWNER;
import static tripster.tripster.Constants.USER_ABOUT_K;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class UserProfileFragment extends Fragment {
  private static final String TAG = UserProfileFragment.class.getName();
  public static final String ADD = "Add friend";
  public static final String FRIENDS = "Friends";
  public static final String REQUEST_SENT = "Request sent";

  private UserTripsAdapter tripsAdapter;
  private TransactionManager tM;
  private String userId;
  private String friendshipId = UUID.randomUUID().toString();
  private LiveQuery tripsLQ;
  private LiveQuery friendshipLQ;
  private LiveQuery friendsLQ;

  private TextView name;
  private TextView about;
  private ImageView avatar;
  private Button friendshipStatusButton;
  private Button noOfFriendsButton;
  private Button noOfTripsButton;
  private GridView grid;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_trips, container, false);
    userId = getArguments().getString("userId");
    tM = new TransactionManager(getContext());

    name = (TextView) view.findViewById(R.id.userName);
    about = (TextView) view.findViewById(R.id.userAbout);
    avatar = (ImageView) view.findViewById(R.id.avatar);
    friendshipStatusButton = (Button) view.findViewById(R.id.friendStatus);
    noOfFriendsButton = (Button) view.findViewById(R.id.noOfFriends);
    noOfTripsButton = (Button) view.findViewById(R.id.noOfTrips);
    grid = (GridView) view.findViewById(R.id.myTrips);

    noOfFriendsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tM.accessFriendsOfUser(userId);
      }
    });

    if (userId.equals(currentUserId)) {
      about.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setTitle("Let others know who you are!");

          final EditText input = new EditText(getActivity());
          input.setInputType(InputType.TYPE_CLASS_TEXT);
          builder.setView(input);
          builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Map<String, Object> props = new HashMap<>();
              props.put(USER_ABOUT_K, input.getText().toString());
              tDb.upsertNewDocById(userId, props);
            }
          });
          builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.cancel();
            }
          });

          builder.show();
          return false;
        }
      });
    } else {
      friendshipStatusButton.setVisibility(View.VISIBLE);
      friendshipStatusButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addFriendship();
        }
      });
      changeFriendshipButton(ADD);
    }

    return view;
  }

  private void addFriendship() {
    Document friendship = tDb.getDb().getDocument(friendshipId);
    String level = (String) friendship.getProperty(FS_LEVEL_K);
    Map<String, Object> props = new HashMap<>();
    if (level != null) {
      if ((friendship.getProperty(FS_SENDER_K).equals(userId) &&
          friendship.getProperty(FS_RECEIVER_K).equals(currentUserId)) &&
          !level.equals(FS_LEVEL_CONFIRMED)) {
        props.put(FS_LEVEL_K, FS_LEVEL_CONFIRMED);
      } else {
        Log.e(TAG, "Friendship is in a weird state :((");
      }
    } else {
      props.put(FS_SENDER_K, currentUserId);
      props.put(FS_RECEIVER_K, userId);
      props.put(FS_TIME_K, System.currentTimeMillis());
      props.put(FS_LEVEL_K, FS_LEVEL_SENT);
    }
    tDb.upsertNewDocById(friendship.getId(), props);
  }

  @Override
  public void onResume() {
    super.onResume();
    tDb.getDocumentById(userId).addChangeListener(userChangedListener);
    restartTripsLiveQuery();
    restartFriendshipLiveQuery();
    restartFriendsLiveQuery();
  }

  private void restartFriendsLiveQuery() {
    Query q = tDb.getDb().getExistingView(FRIENDS_BY_USER).createQuery();
    q.setKeys(Collections.singletonList((Object) currentUserId));
    friendsLQ = q.toLiveQuery();
    friendsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        noOfFriendsButton.setText(event.getRows().getCount());
      }
    });
    friendsLQ.start();
  }

  private void restartFriendshipLiveQuery() {
    Query q = tDb.getDb().getExistingView(TRIPS_BY_OWNER).createQuery();
    List<Object> keys = new ArrayList<>(2);
    List<String> key = new ArrayList<>();
    key.add(currentUserId);
    key.add(userId);
    keys.add(key);
    key = new ArrayList<>();
    key.add(userId);
    key.add(currentUserId);
    keys.add(key);
    q.setKeys(keys);
    friendshipLQ = q.toLiveQuery();
    friendshipLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        int count = event.getRows().getCount();
        if (count == 0) {
          Log.d(TAG, "We do get notfied even when no rows");
        } else if (count == 1) {
          Document fs = event.getRows().getRow(0).getDocument();
          friendshipId = fs.getId();
          String level = (String) fs.getProperty(FS_LEVEL_K);
          String sender = (String) fs.getProperty(FS_SENDER_K);
          if (level.equals(FS_LEVEL_CONFIRMED)) {
            changeFriendshipButton(FRIENDS);
          } else if (sender.equals(currentUserId)) {
            changeFriendshipButton(REQUEST_SENT);
          }
        } else if (count > 1) {
          Log.e(TAG, "Inconsistent state of friendship between: " + currentUserId + " and " + userId);
        }
      }
    });
    friendshipLQ.start();
  }

  private void restartTripsLiveQuery() {
    Query q = tDb.getDb().getExistingView(TRIPS_BY_OWNER).createQuery();
    q.setKeys(Collections.singletonList((Object) userId));
    tripsLQ = q.toLiveQuery();
    tripsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        List<Pair<Long, String>> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          Pair<Long, String> p = new Pair<>((Long) r.getValue(), r.getDocumentId());
          results.add(p);
        }
        Collections.sort(results, new Comparator<Pair<Long, String>>() {
          @Override
          public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
            return o2.first.compareTo(o1.first);
          }
        });
        noOfTripsButton.setText(results.size());
        initItemGridAdapter(results);
      }
    });
    tripsLQ.start();
  }

  private Document.ChangeListener userChangedListener = new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      Document userDoc = tDb.getDocumentById(event.getChange().getDocumentId());

      name.setText((String) userDoc.getProperty(USER_NAME_K));

      String aboutUser = (String) userDoc.getProperty(USER_ABOUT_K);
      if (aboutUser != null) {
        about.setText(aboutUser);
      } else if (userId.equals(currentUserId)) {
        about.setText("Long touch to add something about you...");
      } else {
        about.setVisibility(View.GONE);
      }

      new Image((String) userDoc.getProperty(USER_AVATAR_K)).displayIn(avatar);
    }
  };

  @Override
  public void onPause() {
    try {
      tDb.getDocumentById(userId).removeChangeListener(userChangedListener);
      tripsLQ.stop();
      friendshipLQ.stop();
      friendsLQ.stop();
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

    tripsAdapter = new UserTripsAdapter(
        getContext(),
        R.layout.trips_grid_item,
        R.id.tripName,
        trips);
    grid.setAdapter(tripsAdapter);
  }

  private void changeFriendshipButton(String friendshipStatus) {
    if (!friendshipStatus.equals(ADD)) {
      friendshipStatusButton.setEnabled(false);
    } else {
      friendshipStatusButton.setEnabled(true);
    }
    friendshipStatusButton.setText(friendshipStatus);
  }
}

