package tripster.tripster.adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;

public class FriendsListAdapter extends ArrayAdapter<Pair<String, String>> {

  private final Activity activity;
  private final List<Pair<String, String>> friends;

  public FriendsListAdapter(Activity activity, List<Pair<String, String>> friends) {
    super(activity, R.layout.friends_list_item, friends);
    this.activity = activity;
    this.friends = friends;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.friends_list_item, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.friend_name);
    txtTitle.setText(friends.get(position).second);
    return rowView;
  }
}
