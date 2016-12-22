package tripster.tripster.UILayer.trip.timeline;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tripster.tripster.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static tripster.tripster.Constants.PHOTO_PATH_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

class TimeLineViewHolder extends RecyclerView.ViewHolder {
  TextView locationTextView;
  private TimelineView timelineView;
  private View itemView;

  TimeLineViewHolder(View itemView, int viewType) {
    super(itemView);
    this.itemView = itemView;
    locationTextView = (TextView) itemView.findViewById(R.id.tx_name);
    timelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
    timelineView.initLine(viewType);
  }

  void initView(List<String> photos) {
    LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.linear);
    layout.removeAllViews();
    for (String photoId : photos) {
      String photoUri = (String) tDb.getDocumentById(photoId).getProperty(PHOTO_PATH_K);
      ImageView imageView = new ImageView(itemView.getContext());
      imageView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
      Picasso
          .with(imageView.getContext())
          .load(photoUri)
          .resize(300, 300)
          .centerInside()
          .into(imageView);
      layout.addView(imageView);
    }
  }
}

