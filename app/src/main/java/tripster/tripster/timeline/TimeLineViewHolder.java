package tripster.tripster.timeline;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.trips.pictures.Photo;
import tripster.tripster.trips.tabs.Event;

/**
 * Created by HP-HP on 05-12-2015.
 */
class TimeLineViewHolder extends RecyclerView.ViewHolder {
    public TextView locationTextView;
    private TimelineView timelineView;
    private View itemView;

    TimeLineViewHolder(View itemView, int viewType) {
        super(itemView);
        this.itemView = itemView;
        locationTextView = (TextView) itemView.findViewById(R.id.tx_name);
        timelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
        timelineView.initLine(viewType);
    }

    public void initView(Event event) {
        LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.linear);
        List<String> photosURIs = event.getPhotoUris();
        if (photosURIs.isEmpty()) {
            CardView cardView =  (CardView) itemView.findViewById(R.id.cardView);
            int width = (int) itemView.getResources().getDimension(R.dimen.timeline_cardview_width);
            int height = (int) itemView.getResources().getDimension(R.dimen.timeline_cardview_height_no_photos);
            Resources r = itemView.getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            lp.setMargins(px, px, px, px);
            cardView.setLayoutParams(lp);
        } else {
            for (String photoUri : photosURIs) {
                ImageView imageView = new ImageView(itemView.getContext());
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Picasso.with(imageView.getContext())
                        .load(photoUri).resize(300, 300).centerInside().into(imageView);
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Are you sure you want to remove photo?")
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        return true;
                    }
                });
//            Photo photo = new Photo(photoUri, "");
//            photo.displayIn(imageView);
                layout.addView(imageView);
            }
        }
    }
}
