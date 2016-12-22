package tripster.tripster.UILayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import tripster.tripster.R;
import tripster.tripster.UILayer.trip.timeline.TimelineFragment;
import tripster.tripster.UILayer.users.SearchForUsersFragment;
import tripster.tripster.UILayer.users.UserProfileFragment;
public class TransactionManager {

  private AppCompatActivity context;

  public TransactionManager(Context context) {
    this.context = (AppCompatActivity) context;
  }

  public void accessTrip(String tripId) {
    // Change to the corresponding TripFragment.
    TimelineFragment frag = new TimelineFragment();
    Bundle arguments = new Bundle();
    arguments.putString("tripId", tripId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  public void accessFriendsOfUser(String userId) {
    // Change to the corresponding TripFragment.
    SearchForUsersFragment frag = new SearchForUsersFragment();
    Bundle arguments = new Bundle();
    arguments.putString("userId", userId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  public void accessUser(String userId) {
    // Change to the corresponding UserProfileFragment.
    UserProfileFragment frag = new UserProfileFragment();
    Bundle arguments = new Bundle();
    arguments.putString("userId", userId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  private void accessFragment(Fragment frag) {
    FragmentTransaction trans = context.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }
}
