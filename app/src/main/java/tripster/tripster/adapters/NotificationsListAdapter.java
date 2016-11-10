package tripster.tripster.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;

public class NotificationsListAdapter extends ArrayAdapter<String> {

  private static final String TAG = NotificationsListAdapter.class.getName();
  private final Activity activity;
  private final List<String> userNames;

  public NotificationsListAdapter(Activity activity, List<String> userNames) {
    super(activity, R.layout.friends_list_item, userNames);

    this.activity = activity;
    this.userNames = userNames;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.notifications_list_item, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.notification_text);
    Button acceptButton = (Button) rowView.findViewById(R.id.accept_button);
    Button declineButton = (Button) rowView.findViewById(R.id.decline_button);

    txtTitle.setText(userNames.get(position) + "has sent you a friend request");
    setAcceptClickListener(acceptButton);
    setDeclineClickListener(declineButton);
    return rowView;
  }

  private void setAcceptClickListener(Button acceptButton) {
    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendAcceptRequestToServer();
      }
    });
  }

  private void sendAcceptRequestToServer() {
    Log.d(TAG, "Sent accept request");

  }

  private void setDeclineClickListener(Button acceptButton) {
    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDeclineRequestToServer();
      }
    });
  }

  private void sendDeclineRequestToServer() {
    Log.d(TAG, "Sent decline request");
  }
}
