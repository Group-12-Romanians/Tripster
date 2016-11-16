package tripster.tripster.friends;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.User;

public class FriendsListAdapter extends ArrayAdapter<User> {

  private final Activity activity;
  private final List<User> friends;

  public FriendsListAdapter(Activity activity, List<User> friends) {
    super(activity, R.layout.friends_list_item, friends);
    this.activity = activity;
    this.friends = friends;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.friends_list_item, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.friend_name);
    txtTitle.setText(friends.get(position).getName());
    return rowView;
  }
}
