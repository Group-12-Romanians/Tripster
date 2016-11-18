package tripster.tripster.trips.tabs.timeline;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import tripster.tripster.R;

/**
 * Created by HP-HP on 05-12-2015.
 */
public class TimeLineViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TimelineView mTimelineView;

    public TimeLineViewHolder(View itemView, int viewType) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.tx_name);
        mTimelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
        mTimelineView.initLine(viewType);

        LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.linear);
        for (int i = 0; i < 20; i++) {
            ImageView imageView = new ImageView(itemView.getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Picasso.with(itemView.getContext()).load("https://pbs.twimg.com/profile_images/447374371917922304/P4BzupWu.jpeg")
                    .resize(300, 300).centerInside().into(imageView);
            layout.addView(imageView);
        }

    }
}
