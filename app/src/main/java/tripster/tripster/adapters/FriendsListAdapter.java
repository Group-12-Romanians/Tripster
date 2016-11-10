package tripster.tripster.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tripster.tripster.R;

public class FriendsListAdapter extends ArrayAdapter<String> {

  private final Activity activity;
  private final List<String> friendsNames;

  public FriendsListAdapter(Activity activity, List<String> friendsNames) {
    super(activity, R.layout.friends_list_item, friendsNames);

    this.activity = activity;
    this.friendsNames = friendsNames;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View rowView = inflater.inflate(R.layout.photos_list, null,true);
    TextView txtTitle = (TextView) rowView.findViewById(R.id.photo_info);

    txtTitle.setText(friendsNames.get(position));
    return rowView;
  }
}
