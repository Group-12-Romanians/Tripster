package tripster.tripster.UILayer.users;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.trip.timeline.TimelineFragment;
import tripster.tripster.dataLayer.TripsterDb;
import tripster.tripster.dataLayer.events.FriendsChangedEvent;
import tripster.tripster.dataLayer.events.TripsChangedEvent;
import tripster.tripster.dataLayer.events.UsersChangedEvent;

import static tripster.tripster.UILayer.TripsterActivity.USER_ID;

public class UserProfileFragment extends Fragment {
  private static final String TAG = UserProfileFragment.class.getName();
  public static final String ADD = "Add friend";
  public static final String FRIENDS = "Friends";
  public static final String REQUEST_SENT = "Request sent";

  private UserTripsAdapter tripsAdapter;
  String userId;
  private GridView grid;
  private Button friendshipStatusButton;
  private String friendshipId;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_trips, container, false);
    // Get userId from bundle.
    userId = this.getArguments().getString("userId");
    grid = (GridView) view.findViewById(R.id.myTrips);
    // Initial friendshipId
    friendshipId = UUID.randomUUID().toString();

    view.findViewById(R.id.noOfFriends).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SearchForUsersFragment frag = new SearchForUsersFragment();
        Bundle arguments = new Bundle();
        arguments.putString("userId", userId);
        frag.setArguments(arguments);
        FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
        trans.addToBackStack("");
        trans.commit();
      }
    });

    friendshipStatusButton = (Button) view.findViewById(R.id.friendStatus);
    if (!userId.equals(USER_ID)) {
      friendshipStatusButton.setVisibility(View.VISIBLE);

      changeFriendshipButton(ADD);
      friendshipStatusButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addFriendship(USER_ID, userId);
        }
      });
    }
    if (userId.equals(USER_ID)) {
      view.findViewById(R.id.userAbout).setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setTitle("Title");

          final EditText input = new EditText(getActivity());
          input.setInputType(InputType.TYPE_CLASS_TEXT);
          builder.setView(input);
          builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Map<String, Object> props = new HashMap<>();
              props.put("about", input.getText().toString());
              TripsterDb.getInstance().upsertNewDocById(userId, props);
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
    }
    return view;
  }

  private void addFriendship(String me, String otherUser) {
    Document friendship = TripsterDb.getInstance().getHandle().getDocument(friendshipId);
    String level = (String) friendship.getProperty("level");
    Map<String, Object> props = new HashMap<>();
    if (level != null) {
      if ((friendship.getProperty("sender").equals(otherUser) &&
          friendship.getProperty("receiver").equals(me)) &&
          (friendship.getProperty("level").equals("declined") ||
          friendship.getProperty("level").equals("sent"))) {
        props.put("level", "confirmed");
      } else {
        Log.e(TAG, "Friendship is in a weird state :((");
      }
    } else {
      props.put("sender", me);
      props.put("receiver", otherUser);
      props.put("level", "sent");
    }
    TripsterDb.getInstance().upsertNewDocById(friendship.getId(), props);
  }

  @Override
  public void onResume() {
    Log.d(TAG, "Register fragment");
    initItemGridAdapter();
    EventBus.getDefault().register(this);
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "UnRegister fragment");
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  private void initItemGridAdapter() {
    tripsAdapter = new UserTripsAdapter(
        getContext(),
        R.layout.trips_grid_item,
        R.id.tripName,
        new ArrayList<Document>()
    );
    grid.setAdapter(tripsAdapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "I clicked a trip, I want to see it :)");
        UserTripsAdapter.ViewHolderTripPreview holder = (UserTripsAdapter.ViewHolderTripPreview) view.getTag();
        accessTrip(holder.doc);
      }
    });
  }

  private void accessTrip(Document tripDoc) {
    // Change to the corresponding TripFragment.
    TimelineFragment frag = new TimelineFragment();
    Bundle arguments = new Bundle();
    arguments.putString("tripId", tripDoc.getId());
    arguments.putString("userId", userId);
    frag.setArguments(arguments);
    FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }

  //-----------------------------EVENTS--------------------------------------//
  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onUsersChangedEvent(UsersChangedEvent event) {
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    View view = getView();
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow change = changes.getRow(i);
      Document userDoc = change.getDocument();
      // Get only the document corresponding to the current user.
      if ((userDoc.getId()).equals(userId)) {
        String userName = (String) userDoc.getProperty("name");
        String avatarUrl = (String) userDoc.getProperty("avatarUrl");
        String about = (String) userDoc.getProperty("about");

        // Set avatar, about and user name according to the document.
        ((TextView) view.findViewById(R.id.userName)).setText(userName);
        if (about != null) {
          ((TextView) view.findViewById(R.id.userAbout)).setText(about);
        } else if (!userId.equals(USER_ID)){
          view.findViewById(R.id.userAbout).setVisibility(View.GONE);
        } else if (userId.equals(USER_ID)){
          ((TextView) view.findViewById(R.id.userAbout)).setText("Long touch to add something about you.");
        }
        new Image(avatarUrl, "").displayIn(((ImageView) view.findViewById(R.id.avatar)));
        break;
      }
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onTripsChangedEvent(TripsChangedEvent event) {
    Log.d(TAG, "In trips change");
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    View view = getView();
    tripsAdapter.clear();
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      Document tripDoc = row.getDocument();
      String ownerId = (String) tripDoc.getProperty("ownerId");
      // Get only the documents corresponding to the current user.
      if (ownerId.equals(userId)) {
        tripsAdapter.add(tripDoc);
      }
    }
    // Set the number of trips and update the grid.
    ((Button) view.findViewById(R.id.noOfTrips)).setText(String.valueOf(tripsAdapter.getCount()));
    tripsAdapter.notifyDataSetChanged();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onFriendsChangedEvent(FriendsChangedEvent event) {
    int noOfFriends = 0;
    QueryEnumerator enumerator = event.getEvent().getRows();

    for (int i = 0; i < enumerator.getCount(); i++) {
      QueryRow row = enumerator.getRow(i);
      if (row.getKey().equals("confirmed")) {
        Document friendshipDoc = row.getDocument();
        if (friendshipDoc.getProperty("sender").equals(userId)) {
          if (friendshipDoc.getProperty("receiver").equals(USER_ID)) {
            changeFriendshipButton(FRIENDS);
          }
          noOfFriends++;
        } else if (friendshipDoc.getProperty("receiver").equals(userId)) {
          if (friendshipDoc.getProperty("sender").equals(USER_ID)) {
            changeFriendshipButton(FRIENDS);
          }
          noOfFriends++;
        }
      } else {
        Log.d(TAG, "Level is sent");
        Document friendshipDoc = row.getDocument();
        if (friendshipDoc.getProperty("sender").equals(USER_ID)) {
          if (friendshipDoc.getProperty("receiver").equals(userId)) {
            Log.d(TAG, "I am the sender");
            changeFriendshipButton(REQUEST_SENT);
            friendshipId = friendshipDoc.getId();
          }
        } else if (friendshipDoc.getProperty("receiver").equals(USER_ID)) {
          if (friendshipDoc.getProperty("sender").equals(userId)) {
            Log.d(TAG, "The other is the sender");
            changeFriendshipButton(ADD);
            friendshipId = friendshipDoc.getId();
          }
        }
      }
    }
    ((Button) getView().findViewById(R.id.noOfFriends)).setText(String.valueOf(noOfFriends));
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

