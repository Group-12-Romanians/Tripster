package tripster.tripster.UILayer.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.FS_LEVEL_CONFIRMED;
import static tripster.tripster.Constants.FS_LEVEL_DECLINED;
import static tripster.tripster.Constants.FS_SENDER_K;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class NotificationsAdapter extends ArrayAdapter<String> {
  private static final String TAG = NotificationsAdapter.class.getName();

  private List<String> requests;
  private TransactionManager tM;
  private LayoutInflater inflater;

  NotificationsAdapter(Context context, int resource, int textViewResourceId, List<String> requests) {
    super(context, resource, textViewResourceId, requests);
    this.requests = requests;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    tM = new TransactionManager(getContext());
  }

  private class ViewHolder {
    ImageView requesterPhoto;
    TextView notificationText;
    Button accept;
    Button decline;
  }

  @Override
  public int getCount() {
    return requests.size();
  }

  @Override
  public String getItem(int position) {
    return requests.get(position);
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
      holder.requesterPhoto = (ImageView) convertView.findViewById(R.id.userPhoto);
      holder.notificationText = (TextView) convertView.findViewById(R.id.notification_text);
      holder.accept = (Button) convertView.findViewById(R.id.accept_button);
      holder.decline = (Button) convertView.findViewById(R.id.decline_button);
      convertView.setTag(holder);
    }
    try {
      final String requestId = requests.get(position);
      final Document requestDocument = tDb.getDocumentById(requestId);
      final String userId = (String) requestDocument.getProperty(FS_SENDER_K); //TODO: add more types of notifications
      final Document userDoc = tDb.getDocumentById(userId);

      // Listeners
      View.OnClickListener userClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          tM.accessUser(userId);
        }
      };

      // Set user name.
      TextView requesterName = ((ViewHolder) convertView.getTag()).notificationText;
      requesterName.setOnClickListener(userClickListener);
      requesterName.setText((String) userDoc.getProperty(USER_NAME_K));

      // Set user picture.
      ImageView requesterPhoto = ((ViewHolder)convertView.getTag()).requesterPhoto;
      Image image = new Image((String) userDoc.getProperty(USER_AVATAR_K));
      requesterPhoto.setOnClickListener(userClickListener);
      image.displayIn(requesterPhoto);

      // Set action listeners.
      ((ViewHolder)convertView.getTag()).accept.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          updateRequest(requestId, true);
        }
      });
      ((ViewHolder) convertView.getTag()).decline.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          updateRequest(requestId, false);
        }
      });
    } catch (Exception e) {
      Log.e(TAG, "Cannot display friend request");
      e.printStackTrace();
    }
    return convertView;
  }

  private void updateRequest(String friendshipId, boolean accept) {
    Document friendship = tDb.getDocumentById(friendshipId);
    Map<String, Object> props = new HashMap<>();
    props.put("level", accept ? FS_LEVEL_CONFIRMED : FS_LEVEL_DECLINED);
    tDb.upsertNewDocById(friendship.getId(), props);
  }
}
