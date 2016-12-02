package tripster.tripster.UILayer.users;

import android.app.Activity;
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

import tripster.tripster.Photo;
import tripster.tripster.R;

public class SearchableAdapter extends BaseAdapter implements Filterable {

  private static final String TAG = SearchableAdapter.class.getName();
  private final List<Document> originalData;
  private List<Document> filteredData;
  private Activity activity;
  private Filter itemFilter;

  public SearchableAdapter(Activity activity, List<Document> data) {
    this.filteredData = data;
    this.originalData = data;
    this.activity = activity;
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

  public class ViewHolder {
    TextView text;
    ImageView image;
    Document doc;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row.
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to reinflate it.
    if (convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.users_list_item, null);

      // Creates a ViewHolder and store references to the two children views
      // we want to bind data to.
      holder = new ViewHolder();
      holder.text = (TextView) convertView.findViewById(R.id.friend_item_view);
      holder.image = (ImageView) convertView.findViewById(R.id.friend_pic);
      holder.doc = filteredData.get(position);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    String userName = (String) filteredData.get(position).getProperty("name");
    holder.text.setText(userName);
    new Photo((String) filteredData.get(position).getProperty("avatarUrl"), "").displayIn(holder.image);
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
      List<Document> results = new ArrayList<>();
      for (Document candidateData : originalData) {
        String candidate = (String) candidateData.getProperty("name");
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
      filteredData = (List<Document>) results.values;
      notifyDataSetChanged();
    }
  }
}
