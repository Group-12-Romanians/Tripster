package tripster.tripster.UILayer.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.NOT_FOLLOWER;
import static tripster.tripster.Constants.NOT_FOLLOWER_K;
import static tripster.tripster.Constants.NOT_RESTAURANT;
import static tripster.tripster.Constants.NOT_RESTAURANT_K;
import static tripster.tripster.Constants.NOT_RESTAURANT_PIC_K;
import static tripster.tripster.Constants.NOT_TIME_K;
import static tripster.tripster.Constants.NOT_TYPE_K;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class NotificationsAdapter extends ArrayAdapter<String> {
  private static final String TAG = NotificationsAdapter.class.getName();

  private List<String> notifications;

  private TransactionManager tM;
  private LayoutInflater inflater;

  NotificationsAdapter(Context context, int resource, int textViewResourceId, List<String> notifications) {
    super(context, resource, textViewResourceId, notifications);
    this.notifications = notifications;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    tM = new TransactionManager(getContext());
  }

  private class ViewHolder {
    ImageView notificationPhoto;
    TextView notificationText;
    TextView notificationTime;
  }

  @Override
  public int getCount() {
    return notifications.size();
  }

  @Override
  public String getItem(int position) {
    return notifications.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.notifications_list_item, null);
      ViewHolder holder = new ViewHolder();
      holder.notificationPhoto = (ImageView) convertView.findViewById(R.id.notification_photo);
      holder.notificationText = (TextView) convertView.findViewById(R.id.notification_text);
      holder.notificationTime = (TextView) convertView.findViewById(R.id.notification_time);
      convertView.setTag(holder);
    }
    try {
      final String notId = notifications.get(position);
      final Document notDoc = tDb.getDocumentById(notId);

      if (notDoc.getProperty(NOT_TYPE_K).equals(NOT_FOLLOWER)) {
        handleFollowRequest(convertView, notDoc);
      } else if (notDoc.getProperty(NOT_TYPE_K).equals(NOT_RESTAURANT)) {
        handleRestaurantNotification(convertView, notDoc);
      }
      TextView notTime = ((ViewHolder) convertView.getTag()).notificationTime;
      CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
          (Long) notDoc.getProperty(NOT_TIME_K),
          System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
      notTime.setText(timeAgo);
    } catch (Exception e) {
      Log.e(TAG, "Cannot display friend request");
      e.printStackTrace();
    }
    return convertView;
  }

  private View handleRestaurantNotification(final View convertView, final Document notDoc) {
    final String restaurantLink = (String) notDoc.getProperty(NOT_RESTAURANT_K);

    // Listeners
    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          notDoc.delete();
        } catch (CouchbaseLiteException e) {
          Log.e(TAG, "Could not delete doc.");
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(restaurantLink));
        convertView.getContext().startActivity(browserIntent);
      }
    });

    // Set user name.
    TextView notText = ((ViewHolder) convertView.getTag()).notificationText;
    String text = "How about some food?\n Click to check out this restaurant.";
    notText.setText(text);

    // Set user picture.
    ImageView notPhoto = ((ViewHolder)convertView.getTag()).notificationPhoto;
    new Image((String) notDoc.getProperty(NOT_RESTAURANT_PIC_K)).displayIn(notPhoto);
    return  convertView;
  }

  private View handleFollowRequest(View convertView, final Document notDoc) {
    final String followerId = (String) notDoc.getProperty(NOT_FOLLOWER_K);
    final Document userDoc = tDb.getDocumentById(followerId);

    // Listeners
    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          notDoc.delete();
        } catch (CouchbaseLiteException e) {
          Log.e(TAG, "Could not delete doc.");
        }
        tM.accessUser(followerId);
      }
    });

    // Set user name.
    TextView notText = ((ViewHolder) convertView.getTag()).notificationText;
    String text = userDoc.getProperty(USER_NAME_K) + " started to follow you.\nClick to set a visibility level!";
    notText.setText(text);

    // Set user picture.
    ImageView notPhoto = ((ViewHolder)convertView.getTag()).notificationPhoto;
    new Image((String) userDoc.getProperty(USER_AVATAR_K)).displayIn(notPhoto);
    return  convertView;
  }
}
