package tripster.tripster.friends;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;
import tripster.tripster.TripsterActivity;
import tripster.tripster.User;

public class SearchableAdapter extends BaseAdapter implements Filterable {

  private static final String TAG = SearchableAdapter.class.getName();
  private static final String FRIEND_REQUEST_URL
      = TripsterActivity.SERVER_URL + "/friend_request";
  private final List<User> originalData;
  private List<User> filteredData;
  private LayoutInflater inflater;
  private Filter itemFilter;

  public SearchableAdapter(Context context, List<User> data) {
    this.filteredData = data;
    this.originalData = data;
    inflater = LayoutInflater.from(context);
    itemFilter = new ItemFilter();
  }

  @Override
  public int getCount() {
    return filteredData.size();
  }

  @Override
  public Object getItem(int position) {
    return filteredData.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  static class ViewHolder {
    TextView text;
    Button addFriendButton;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row.
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to reinflate it.
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.users_list_item, null);

      // Creates a ViewHolder and store references to the two children views
      // we want to bind data to.
      holder = new ViewHolder();
      holder.text
          = (TextView) convertView.findViewById(R.id.friend_item_view);
      holder.addFriendButton
          = (Button) convertView.findViewById(R.id.add_friend_button);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    String userName = filteredData.get(position).getName();
    holder.text.setText(userName);

    if (!isFriend(filteredData.get(position))) {
      holder.addFriendButton.setVisibility(View.VISIBLE);
      String friendId = filteredData.get(position).getId();
      holder
          .addFriendButton
          .setOnClickListener(getFriendRequestListener(friendId,
                                                       holder.addFriendButton));
    } else if (isFriendRequestSent(filteredData.get(position))) {
      holder.addFriendButton.setVisibility(View.VISIBLE);
      holder.addFriendButton.setText("Request Sent");
      holder.addFriendButton.setClickable(false);
    } else if (isFriend(filteredData.get(position))) {
      holder.addFriendButton.setVisibility(View.INVISIBLE);
    }

    return convertView;
  }

  @Override
  public Filter getFilter() {
    return itemFilter;
  }

  private class ItemFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      String filterString = constraint.toString().toLowerCase();
      List<User> results = new ArrayList<>();
      for (User candidateData : originalData) {
        String candidate = candidateData.getName();
        if (candidate.toLowerCase().contains(filterString)) {
          results.add(candidateData);
        }
      }
      FilterResults filterResults = new FilterResults();
      filterResults.values = results;
      filterResults.count = results.size();
      return filterResults;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      filteredData = (List<User>) results.values;
      notifyDataSetChanged();
    }
  }

  private boolean isFriend(User user) {
    return FriendsFragment.friends.contains(user);
  }

  private boolean isFriendRequestSent(User user) {
    return FriendsFragment.friendRequests.contains(user);
  }

  private View.OnClickListener getFriendRequestListener(final String friendId,
                                                        final Button friendRequestButton) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        friendRequestButton.setText("Friend request sent");
        sendFriendRequest(friendId);
      }
    };
  }

  private void sendFriendRequest(final String friendId) {
    Log.d(TAG, TripsterActivity.USER_ID);
    String friendRequestUrl = FRIEND_REQUEST_URL
        + "?user_id="
        + TripsterActivity.USER_ID;

    StringRequest friendRequest = new StringRequest(Request.Method.POST, friendRequestUrl,
      new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          Log.d(TAG, "Friend request sent");
        }
      },
      new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.d(TAG, "Unable to send friend request.");
        }
      }) {
        @Override
        public Map<String, String> getParams() {
          Map<String, String> params = new HashMap<>();
          Log.d(TAG, "Parameters were assigned");
          Log.d(TAG, "Friend id is: " + friendId.replace("\"", ""));
          params.put("friend", friendId.replace("\"", ""));
          return params;
        }
    };
    TripsterActivity.reqQ.add(friendRequest);
  }
}
