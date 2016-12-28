package tripster.tripster.UILayer.search;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;

import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class SearchableAdapter extends BaseAdapter implements Filterable {
  private static final String TAG = SearchableAdapter.class.getName();

  private final List<String> originalData;
  private List<String> filteredData;
  private TransactionManager tM;
  private LayoutInflater inflater;
  private Filter itemFilter;

  SearchableAdapter(Activity activity, List<String> data) {
    this.filteredData = data;
    this.originalData = data;
    itemFilter = new ItemFilter();

    inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    tM = new TransactionManager(activity);
  }

  @Override
  public int getCount() {
    return filteredData.size();
  }

  @Override
  public String getItem(int position) {
    return filteredData.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private class ViewHolder {
    TextView text;
    ImageView image;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.users_list_item, null);
      ViewHolder holder = new ViewHolder();
      holder.text = (TextView) convertView.findViewById(R.id.friend_item_view);
      holder.image = (ImageView) convertView.findViewById(R.id.friend_pic);
      convertView.setTag(holder);
    }
    try {
      final String userId = filteredData.get(position);
      final Document userDoc = tDb.getDocumentById(userId);

      // Listeners
      convertView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          tM.accessUser(userId);
        }
      });

      // Set user name.
      TextView name = ((ViewHolder) convertView.getTag()).text;
      name.setText((String) userDoc.getProperty(USER_NAME_K));

      // Set user picture.
      ImageView requesterPhoto = ((ViewHolder)convertView.getTag()).image;
      new Image((String) userDoc.getProperty(USER_AVATAR_K)).displayIn(requesterPhoto);
    } catch (Exception e) {
      Log.e(TAG, "Cannot display friend request");
      e.printStackTrace();
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
      List<String> results = new ArrayList<>();
      for (String candidateData : originalData) {
        String candidate = (String) tDb.getDocumentById(candidateData).getProperty(USER_NAME_K);
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
      filteredData = (List<String>) results.values;
      notifyDataSetChanged();
    }
  }
}
