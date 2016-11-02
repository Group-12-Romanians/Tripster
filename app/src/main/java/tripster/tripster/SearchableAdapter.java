package tripster.tripster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dragos on 11/1/16.
 */

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private final List<String> originalData;
    private List<String> filteredData;
    private LayoutInflater inflater;
    private Filter itemFilter;

    public SearchableAdapter(Context context, List<String> data) {
        this.filteredData = data;
        this.originalData = data ;
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need to reinflate it.
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.friends_list_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.friend_item_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(filteredData.get(position));

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
            for (String candidate : originalData) {
                if (candidate.toLowerCase().contains(filterString)) {
                    results.add(candidate);
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
