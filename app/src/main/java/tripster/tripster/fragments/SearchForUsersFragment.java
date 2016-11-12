package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.adapters.SearchableAdapter;
import tripster.tripster.TripsterActivity;

public class SearchForUsersFragment extends Fragment {

  private static final String TAG = SearchForUsersFragment.class.getName();
  private final String USERS_URL = TripsterActivity.SERVER_URL + "/all_users";
  private final String FRIENDS_URL = TripsterActivity.SERVER_URL + "/my_friends";
  private final String REQUESTS_URL = TripsterActivity.SERVER_URL + "/notifications/requests";
  private SearchableAdapter searchableAdapter;
  private ListView friendsList;

  private List<Pair<String, String>> allUsersInfo;
  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search_friends, container, false);

    setAllUsersNames();

    EditText searchBar = (EditText) view.findViewById(R.id.search);
    searchBar.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        System.out.println("Text [" + s + "]");
        searchableAdapter.getFilter().filter(s.toString());
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    friendsList = (ListView) view.findViewById(R.id.friends_list);
    return view;
  }

  private void setAllUsersNames() {
    allUsersInfo = new ArrayList<>();
    StringRequest getUserRequest = getAllUsersStringRequest();
    TripsterActivity.reqQ.add(getUserRequest);
  }

  private void setFriendsNames() {
    FriendsFragment.friends.clear();
    StringRequest getFriendsRequest = getFriendsStringRequest();
    TripsterActivity.reqQ.add(getFriendsRequest);
  }

  private void setFriendRequests() {
    FriendsFragment.friendRequests.clear();
    StringRequest getFriendsRequest = getFriendsRequestsStringRequest();
    TripsterActivity.reqQ.add(getFriendsRequest);
  }

  private StringRequest getAllUsersStringRequest() {
    return new StringRequest(
        Request.Method.GET,
        USERS_URL,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, response);
            try {
              JSONArray users = new JSONArray(response);
              for(int i= 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userId = user.getString("_id");
                String userName = user.getString("name");
                if (!userId.equals(TripsterActivity.USER_ID)) {
                  allUsersInfo.add(new Pair<>(userId, userName));
                }
              }
              setFriendsNames();
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get the list of all users.");
          }
        });

  }

  private StringRequest getFriendsStringRequest() {
    String friendsUrl = FRIENDS_URL
        + "?user_id="
        + TripsterActivity.USER_ID;
    return new StringRequest(
        Request.Method.GET,
        friendsUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Got friends: " + response);
            try {
              JSONArray friends = new JSONArray(response);
              for(int i= 0; i < friends.length(); i++) {
                String friendId = friends.getString(i);
                String friendName = getUserName(friendId);
                FriendsFragment.friends.add(new Pair<>(friendId, friendName));
              }
              setFriendRequests();
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get the list of friends.");
          }
        });

  }

  public StringRequest getFriendsRequestsStringRequest() {
    String friendsRequestUrl = REQUESTS_URL
        + "?user_id="
        + TripsterActivity.USER_ID;
    return new StringRequest(
        Request.Method.GET,
        friendsRequestUrl,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Got friend requests: " + response);
            try {
              JSONArray requests = new JSONArray(response);
              for(int i= 0; i < requests.length(); i++) {
                JSONObject potentialFriend = requests.getJSONObject(i);
                String potentialFriendId = potentialFriend.getString("friend1");
                String potentialFriendName = getUserName(potentialFriendId);
                Log.d(TAG, "Potential friend is:" + potentialFriendName + ", id: " + potentialFriendId);
                if (potentialFriendName != null) {
                  FriendsFragment
                      .friendRequests
                      .add(new Pair<>(potentialFriendId, potentialFriendName));
                }
              }
              searchableAdapter = new SearchableAdapter(getActivity(), allUsersInfo);
              friendsList.setAdapter(searchableAdapter);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Unable to get the list of friends.");
          }
        });
  }

  private String getUserName(String userId) {
    for (Pair<String, String> user : allUsersInfo) {
      if (user.first.equals(userId)) {
        return user.second;
      }
    }
    return null;
  }
}
