package tripster.tripster.UILayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import tripster.tripster.R;
import tripster.tripster.UILayer.trip.timeline.MyTripFragment;
import tripster.tripster.UILayer.trip.timeline.TripFragment;
import tripster.tripster.UILayer.users.lists.FollowersFragment;
import tripster.tripster.UILayer.users.lists.FollowingFragment;
import tripster.tripster.UILayer.users.profile.MyProfileFragment;
import tripster.tripster.UILayer.users.profile.ProfileFragment;
import tripster.tripster.UILayer.settings.SettingsFragment;
import tripster.tripster.UILayer.users.profile.UserProfileFragment;

import static tripster.tripster.Constants.TRIP_ID;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.USER_ID;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class TransactionManager {

  private AppCompatActivity context;

  public TransactionManager(Context context) {
    this.context = (AppCompatActivity) context;
  }

  public void accessTrip(String tripId) {
    // Change to the corresponding TripFragment.
    Fragment frag = new TripFragment();
    if (tDb.getDocumentById(tripId).getProperty(TRIP_OWNER_K).equals(currentUserId)) {
      frag = new MyTripFragment();
    }
    Bundle arguments = new Bundle();
    arguments.putString(TRIP_ID, tripId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  public void accessFollowersOfUser(String userId) {
    // Change to the corresponding TripFragment.
    FollowersFragment frag = new FollowersFragment();
    Bundle arguments = new Bundle();
    arguments.putString(USER_ID, userId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  public void accessFollowingOfUser(String userId) {
    // Change to the corresponding TripFragment.
    FollowingFragment frag = new FollowingFragment();
    Bundle arguments = new Bundle();
    arguments.putString(USER_ID, userId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  public void accessVideo(String videoUrl) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    if (videoUrl != null) {
      intent.setDataAndType(Uri.parse(videoUrl), "video/*");
      context.startActivity(Intent.createChooser(intent, "Complete action using"));
    }
  }

  public void accessUser(String userId) {
    // Change to the corresponding ProfileFragment.
    ProfileFragment frag;

    if (userId.equals(currentUserId)) {
      frag = new MyProfileFragment();
    } else {
      frag = new UserProfileFragment();
    }
    Bundle arguments = new Bundle();
    arguments.putString(USER_ID, userId);
    frag.setArguments(arguments);
    accessFragment(frag);
  }

  private void accessFragment(Fragment frag) {
    FragmentTransaction trans = context.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }

  public void accessSettings() {
    FragmentTransaction trans = context.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, new SettingsFragment());
    trans.addToBackStack("");
    trans.commit();
  }
}
