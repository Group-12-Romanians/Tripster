package tripster.tripster.newsFeed.visibility_demo.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tripster.tripster.R;

public class Holder extends RecyclerView.ViewHolder{

    public final TextView positionView;

    public Holder(View itemView) {
        super(itemView);
        positionView = (TextView) itemView.findViewById(R.id.position);
    }
}
