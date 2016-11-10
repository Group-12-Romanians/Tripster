package tripster.tripster.adapters;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
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

public class SearchableAdapter extends BaseAdapter implements Filterable {

  private static final String TAG = SearchableAdapter.class.getName();
  private static final String FRIEND_REQUEST_URL
      = TripsterActivity.SERVER_URL + "/friend_request";
  private final List<Pair<String, String>> originalData;
  private List<Pair<String, String>> filteredData;
  private LayoutInflater inflater;
  private Filter itemFilter;

  public SearchableAdapter(Context context, List<Pair<String, String>> data) {
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

    holder.text.setText(filteredData
        .get(position)
        .second);
    if (!isFriend()) {
      holder.addFriendButton.setVisibility(View.VISIBLE);
      String friendId = filteredData.get(position).first;
      holder
          .addFriendButton
          .setOnClickListener(getFriendRequestListener(friendId,
                                                       holder.addFriendButton));
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
      List<Pair<String, String>> results = new ArrayList<>();
      for (Pair<String, String> candidateData : originalData) {
        String candidate = candidateData.second;
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
      filteredData = (List<Pair<String, String>>) results.values;
      notifyDataSetChanged();
    }
  }

  private boolean isFriend() {
    // TODO: Search list of friends for this.
    return false;
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

    StringRequest friendRequest = new StringRequest(Request.Method.POST,
        friendRequestUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to send friend request. " + error.networkResponse.data);
          }
        }) {

      @Override
      public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Log.d(TAG, "Parameters were assigned");
        Log.d(TAG, "friwnd id is: " + friendId.replace("\"", ""));
        params.put("friend", friendId.replace("\"", ""));
        return params;
      }
    };
    TripsterActivity.reqQ.add(friendRequest);
  }
}
