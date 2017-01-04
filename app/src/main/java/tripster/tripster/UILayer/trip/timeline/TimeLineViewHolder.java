package tripster.tripster.UILayer.trip.timeline;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tripster.tripster.Constants;
import tripster.tripster.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class TimeLineViewHolder extends RecyclerView.ViewHolder {
  private static final String TAG = TimeLineViewHolder.class.getName();
  TextView locationTextView;
  private View itemView;

  TimeLineViewHolder(View itemView, int viewType) {
    super(itemView);
    this.itemView = itemView;
    locationTextView = (TextView) itemView.findViewById(R.id.tx_name);
    TimelineView timelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
    timelineView.initLine(viewType);
  }

  void initView(String tripId, String placeId) {
    LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.linear);
    layout.removeAllViews();
    Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_PLACE).createQuery();
    List<Object> key = new ArrayList<>();
    key.add(tripId);
    key.add(placeId);
    q.setKeys(Collections.<Object>singletonList(key));
    try {
      QueryEnumerator rows = q.run();
      final List<Pair<Long, String>> results = new ArrayList<>();
      for (int i = 0; i < rows.getCount(); i++) {
        QueryRow r = rows.getRow(i);
        results.add(new Pair<>((long) r.getValue(), Constants.getPath(r.getDocumentId())));
      }
      Collections.sort(results, new Comparator<Pair<Long, String>>() {
        @Override
        public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
          return o2.first.compareTo(o1.first);
        }
      });
      List<String> photos = new ArrayList<>();
      for (Pair<Long, String> result : results) {
        photos.add(result.second);
      }
      Log.d(TAG, "Photos are: " + photos);
      for (String photoUri : photos) {
        ImageView imageView = new ImageView(itemView.getContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        imageView.setPadding(10, 10, 0, 0);
        Glide.with(imageView.getContext())
            .load(photoUri)
            .override(600, 600)
            .fitCenter()
            .into(imageView);
        layout.addView(imageView);
      }
    } catch (CouchbaseLiteException e) {
      Log.e(TAG, "Could not run images query.");
      e.printStackTrace();
    }
  }
}

