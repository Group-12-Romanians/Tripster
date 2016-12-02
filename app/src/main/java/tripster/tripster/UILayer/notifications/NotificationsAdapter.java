package tripster.tripster.UILayer.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
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
import tripster.tripster.UILayer.TripsterActivity;
import tripster.tripster.dataLayer.TripsterDb;

public class NotificationsAdapter extends ArrayAdapter {

  private static final String TAG = NotificationsAdapter.class.getName();
  private List<Pair<String, Document>> requests;
  private Context context;

  public NotificationsAdapter(Context context, int resource, int textViewResourceId, List<Pair<String, Document>> objects) {
    super(context, resource, textViewResourceId, objects);
    this.requests = objects;
    this.context = context;
  }

  public class ViewHolderRequestPreview {
    ImageView requesterPhoto;
    TextView notificationText;
    Button accept;
    Button decline;
    Document doc;
    String notificationId;
  }

  @Override
  public int getCount() {
    return requests.size();
  }

  @Override
  public Object getItem(int position) {
    return requests.get(position);
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
      convertView = view.inflate(R.layout.notifications_list_item, null);
      ViewHolderRequestPreview holder = new ViewHolderRequestPreview();
      holder.requesterPhoto = (ImageView) convertView.findViewById(R.id.userPhoto);
      holder.notificationText = (TextView) convertView.findViewById(R.id.notification_text);
      holder.accept = (Button) convertView.findViewById(R.id.accept_button);
      holder.decline = (Button) convertView.findViewById(R.id.decline_button);
      holder.doc = requests.get(position).second;
      holder.notificationId = requests.get(position).first;
      convertView.setTag(holder);
    }

    try {
      final Document requestDocument = requests.get(position).second;
      ViewHolderRequestPreview holder = (ViewHolderRequestPreview )convertView.getTag();
      String requesterNameString = (String) requestDocument.getProperty("name");
      String requesterPhotoUri = (String) holder.doc.getProperty("avatarUrl");

      // Set user name.
      TextView requesterName = ((ViewHolderRequestPreview)convertView.getTag()).notificationText;
      requesterName.setText(requesterNameString);

      // Set user picture.
      ImageView requesterPhoto = ((ViewHolderRequestPreview)convertView.getTag()).requesterPhoto;
      Image image = new Image(requesterPhotoUri, requesterNameString);
      image.displayIn(requesterPhoto);

      // Set buttons.
      Button accept = ((ViewHolderRequestPreview)convertView.getTag()).accept;
      Button decline = ((ViewHolderRequestPreview)convertView.getTag()).decline;

      final String requestId = requests.get(position).first;
      accept.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          updateRequest(requestId, true);
        }
      });
      decline.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          updateRequest(requestId, false);
        }
      });
    } catch (Exception e) {
      Log.d(TAG, "Cannot display friend request");
      e.printStackTrace();
    }

    return convertView;
  }

  private void updateRequest(String friendshipId, boolean accept) {
    Document friendship = TripsterDb.getInstance().getHandle().getDocument(friendshipId);
    String level = (String) friendship.getProperty("level");
    Map<String, Object> props = new HashMap<>();
    if (level != null) {
      if (friendship.getProperty("receiver").equals(TripsterActivity.USER_ID) &&
          friendship.getProperty("level").equals("sent")) {
        if (accept) {
          props.put("level", "confirmed");
        } else {
          props.put("level", "declined");
        }
        TripsterDb.getInstance().upsertNewDocById(friendship.getId(), props);
      } else {
        Log.e(TAG, "Notification is in a weird state :((");
      }
    } else {
      Log.e(TAG, "Notification is invalid :((");
    }
  }
}
