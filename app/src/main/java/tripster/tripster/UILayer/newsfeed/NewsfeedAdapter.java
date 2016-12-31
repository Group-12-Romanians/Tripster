package tripster.tripster.UILayer.newsfeed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.DEFAULT_PREVIEW;
import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_VIDEO_K;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.R.id.tripDescription;
import static tripster.tripster.R.id.userName;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class NewsfeedAdapter extends ArrayAdapter<String> {
  private static final String TAG = NewsfeedAdapter.class.getName();
  private static final int TRANSPARENCY = 90;

  private List<String> friendsTrips;

  private TransactionManager tM;
  private LayoutInflater inflater;

   NewsfeedAdapter(Context context, int resource, int textViewResourceId, List<String> userStories) {
     super(context, resource, textViewResourceId, userStories);
     this.friendsTrips = userStories;
     inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     tM = new TransactionManager(getContext());
  }

  private class ViewHolder {
    // User details
    ImageView userAvatar;
    TextView userName;
    // Trip details
    ImageView tripPreview;
    ImageView playBtnImg;
    TextView tripName;
    TextView tripDescription;
  }

  @Override
  public int getCount() {
    return friendsTrips.size();
  }

  @Override
  public String getItem(int position) {
    return friendsTrips.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.user_story, null);
      ViewHolder holder = new ViewHolder();

      // Set holder fields
      holder.userName = (TextView) convertView.findViewById(userName);
      holder.userAvatar = (ImageView) convertView.findViewById(R.id.userAvatar);
      holder.tripName = (TextView) convertView.findViewById(R.id.tripName);
      holder.tripDescription = (TextView) convertView.findViewById(tripDescription);
      holder.tripPreview = (ImageView) convertView.findViewById(R.id.tripPreview);
      holder.playBtnImg = (ImageView) convertView.findViewById(R.id.playBtImg);
      holder.playBtnImg.setImageAlpha(TRANSPARENCY);
      convertView.setTag(holder);
    }

    try {
      final String tripId = friendsTrips.get(position);
      final Document tripDoc = tDb.getDocumentById(tripId);
      final String videoUrl = (String) tripDoc.getProperty(TRIP_VIDEO_K);
      final String userId = (String) tripDoc.getProperty(TRIP_OWNER_K);
      Document userDoc = tDb.getDocumentById(userId);

      View.OnClickListener userClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          tM.accessUser(userId);
        }
      };
      View.OnClickListener tripClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          tM.accessTrip(tripId);
        }
      };

      // Set username
      TextView userNameView = ((ViewHolder) convertView.getTag()).userName;
      userNameView.setOnClickListener(userClickListener);
      userNameView.setText((String) userDoc.getProperty(USER_NAME_K));

      // Set avatar
      ImageView userAvatarView = ((ViewHolder) convertView.getTag()).userAvatar;
      userAvatarView.setOnClickListener(userClickListener);
      Image avatar = new Image((String) userDoc.getProperty(USER_AVATAR_K));
      avatar.displayIn(userAvatarView);

      // Set trip name
      TextView tripNameView = ((ViewHolder) convertView.getTag()).tripName;
      tripNameView.setOnClickListener(tripClickListener);
      tripNameView.setText((String) tripDoc.getProperty(TRIP_NAME_K));

      // Set trip description
      TextView tripDescriptionView = ((ViewHolder) convertView.getTag()).tripDescription;
      String tripDescription = (String) tripDoc.getProperty(TRIP_DESCRIPTION_K);
      if (tripDescription == null) {
        tripDescriptionView.setVisibility(View.GONE);
      } else {
        tripDescriptionView.setVisibility(View.VISIBLE);
        tripDescriptionView.setOnClickListener(tripClickListener);
        tripDescriptionView.setText(tripDescription);
      }

      // Set trip preview
      ImageView tripPreview = ((ViewHolder) convertView.getTag()).tripPreview;
      String tripPrev = (String) tripDoc.getProperty(TRIP_PREVIEW_K);
      if (tripPrev == null) {
        tripPrev = DEFAULT_PREVIEW;
      }
      new Image(tripPrev).displayIn(tripPreview);
      ((ViewHolder) convertView.getTag()).playBtnImg
          .setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) { tM.accessVideo(videoUrl); }
      });
    } catch (Exception e) {
      Log.e(TAG, "Cannot display trip story");
      e.printStackTrace();
    }
    return convertView;
  }
}
