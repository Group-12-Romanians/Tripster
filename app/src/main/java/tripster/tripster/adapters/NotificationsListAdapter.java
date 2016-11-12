package tripster.tripster.adapters;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;

public class NotificationsListAdapter extends ArrayAdapter<Pair<String, String>> {

  private static final String TAG = NotificationsListAdapter.class.getName();
  private static final String FRIEND_RESPONSE_URL = TripsterActivity.SERVER_URL + "/friend_response";
  private final Activity activity;
  private final List<Pair<String, String>> requestsInfo;

  public NotificationsListAdapter(Activity activity, List<Pair<String, String>> requestsInfo) {
    super(activity, R.layout.notifications_list_item, requestsInfo);
    for (Pair<String, String> request : requestsInfo) {
      Log.d(TAG, "Request from: " + request.second);
    }
    this.activity = activity;
    this.requestsInfo = requestsInfo;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.notifications_list_item, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.notification_text);
    Button acceptButton = (Button) rowView.findViewById(R.id.accept_button);
    Button declineButton = (Button) rowView.findViewById(R.id.decline_button);

    String requesterId = requestsInfo.get(position).first;
    String requesterName = requestsInfo.get(position).second;

    txtTitle.setText(requesterName + "has sent you a friend request");
    setAcceptClickListener(acceptButton, requesterId);
    setDeclineClickListener(declineButton, requesterId);
    return rowView;
  }

  private void setAcceptClickListener(Button acceptButton, final String requesterId) {
    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendAcceptRequestToServer(requesterId);
      }
    });
  }

  private void sendAcceptRequestToServer(final String requesterId) {
    Log.d(TAG, "Sent accept request");
    String friendResponseUrl = FRIEND_RESPONSE_URL
        + "?user_id="
        + TripsterActivity.USER_ID;
    StringRequest acceptFriendRequest = new StringRequest(
        Request.Method.POST,
        friendResponseUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Aceepted friend request: " + response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to accept request." + error.networkResponse.data);
          }
        }) {

      @Override
      public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Log.d(TAG, "Parameters were assigned");
        params.put("friend", requesterId);
        params.put("stat", "confirmed");

        return params;
      }
    };
    TripsterActivity.reqQ.add(acceptFriendRequest);
  }

  private void setDeclineClickListener(Button acceptButton, final String requesterId) {
    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDeclineRequestToServer(requesterId);
      }
    });
  }

  private void sendDeclineRequestToServer(final String requesterId) {
    Log.d(TAG, "Sent decline request");

    String friendResponseUrl = FRIEND_RESPONSE_URL
        + "?user_id="
        + TripsterActivity.USER_ID;
    StringRequest acceptFriendRequest = new StringRequest(
        Request.Method.POST,
        friendResponseUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Declined friend request: " + response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to decline request. " + error.networkResponse.data);
          }
        }) {

      @Override
      public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Log.d(TAG, "Parameters were assigned");
        params.put("friend", requesterId);
        params.put("stat", "unconfirmed");

        return params;
      }
    };
    TripsterActivity.reqQ.add(acceptFriendRequest);
  }
}
