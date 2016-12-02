package tripster.tripster.UILayer.newsfeed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.Photo;
import tripster.tripster.R;
import tripster.tripster.UILayer.trip.TimelineFragment;
import tripster.tripster.UILayer.users.UserProfileFragment;

public class NewsfeedAdapter extends ArrayAdapter {

  private static final String TAG = NewsfeedAdapter.class.getName();
  public static final int TRANSPARENCY = 90;
  private List<Pair<Document, Document>> friendsTrips;
  private Context context;

  public NewsfeedAdapter(Context context, int resource, int textViewResourceId, List<Pair<Document, Document>> friendsTrips) {
    super(context, resource, textViewResourceId, friendsTrips);
    this.friendsTrips = friendsTrips;
    this.context = context;
  }

  public class ViewHolderNewsfeedPreview {
    // User details
    ImageView userAvatar;
    TextView userName;
    Document userDoc;
    // Trip details
    ImageView tripPreview;
    ImageView playBtnImg;
    TextView tripName;
    TextView tripDescription;
    Document tripDoc;
  }

  @Override
  public int getCount() {
    return friendsTrips.size();
  }

  @Override
  public Object getItem(int position) {
    return friendsTrips.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater view
          = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = view.inflate(R.layout.user_story, null);
      ViewHolderNewsfeedPreview holder = new ViewHolderNewsfeedPreview();
      // Set user details
      holder.userName = (TextView) convertView.findViewById(R.id.userName);
      holder.userAvatar = (ImageView) convertView.findViewById(R.id.userAvatar);
      holder.userDoc = friendsTrips.get(position).first;
      // Set trip details
      holder.tripName = (TextView) convertView.findViewById(R.id.tripName);
      holder.tripDescription = (TextView) convertView.findViewById(R.id.tripDescription);
      holder.tripPreview = (ImageView) convertView.findViewById(R.id.tripPreview);
      holder.tripDoc = friendsTrips.get(position).second;
      holder.playBtnImg = (ImageView) convertView.findViewById(R.id.playBtImg);
      convertView.setTag(holder);
    }

    try {
      Pair<Document, Document> newsFeedDocs = friendsTrips.get(position);
      final Document userDoc = newsFeedDocs.first;
      final Document tripDoc = newsFeedDocs.second;

      // Set username
      String userName = (String) userDoc.getProperty("name");
      TextView userNameView = ((ViewHolderNewsfeedPreview) convertView.getTag()).userName;
      userNameView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          accessUser(userDoc);
        }
      });
      userNameView.setText(userName);
      // Set avatar
      String userAvatar = (String) userDoc.getProperty("avatarUrl");
      Photo avatar = new Photo(userAvatar, userName);
      ImageView userAvatarView = ((ViewHolderNewsfeedPreview) convertView.getTag()).userAvatar;
      avatar.displayIn(userAvatarView);
      // Set trip name
      String tripName = (String) tripDoc.getProperty("name");
      TextView tripNameView = ((ViewHolderNewsfeedPreview) convertView.getTag()).tripName;
      tripNameView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          accessTrip(tripDoc);
        }
      });
      tripNameView.setText(tripName);
      // Set trip description
      String tripDescription = (String) tripDoc.getProperty("description");
      TextView tripDescriptionView = ((ViewHolderNewsfeedPreview) convertView.getTag()).tripDescription;
      if (tripDescription == null) {
        tripDescriptionView.setVisibility(View.INVISIBLE);
      } else {
        tripDescriptionView.setVisibility(View.VISIBLE);
        tripDescriptionView.setText(tripDescription);
      }
      // Set trip preview
      String tripPreviewUri = (String) tripDoc.getProperty("preview");
      ImageView tripPreview = ((ViewHolderNewsfeedPreview) convertView.getTag()).tripPreview;
      Photo photo = new Photo(tripPreviewUri, "");
      photo.displayIn(tripPreview);
      // Add play button image view
      ImageView playbtnView = ((ViewHolderNewsfeedPreview) convertView.getTag()).playBtnImg;
      playbtnView.setImageAlpha(TRANSPARENCY);
      playbtnView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          String videoUrl = (String) tripDoc.getProperty("video");
          Log.d(TAG, "Video uri is " +videoUrl );
          if (videoUrl != null) {
            intent.setDataAndType(Uri.parse(videoUrl), "video/*");
            if (getContext() instanceof AppCompatActivity) {
              AppCompatActivity activity = (AppCompatActivity) getContext();
              activity.startActivity(Intent.createChooser(intent, "Complete action using"));
            }
          }
        }
      });
    } catch (Exception e) {
      Log.d(TAG, "Cannot display trip story");
      e.printStackTrace();
    }
    return convertView;
  }

  private void accessTrip(Document tripDoc) {
    // Change to the corresponding TripFragment.
    TimelineFragment frag = new TimelineFragment();
    Bundle arguments = new Bundle();
    arguments.putString("trip_id", tripDoc.getId());
    frag.setArguments(arguments);
    if (getContext() instanceof AppCompatActivity) {
      AppCompatActivity activity = (AppCompatActivity) getContext();
      FragmentTransaction trans = activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
      trans.addToBackStack("");
      trans.commit();
    }
  }

  private void accessUser(Document userDoc) {
    // Change to the corresponding UserProfileFragment.
    UserProfileFragment frag = new UserProfileFragment();
    Bundle arguments = new Bundle();
    arguments.putString("userId", userDoc.getId());
    frag.setArguments(arguments);
    if (getContext() instanceof AppCompatActivity) {
      AppCompatActivity activity = (AppCompatActivity) getContext();
      FragmentTransaction trans = activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
      trans.addToBackStack("");
      trans.commit();
    }
  }
}
