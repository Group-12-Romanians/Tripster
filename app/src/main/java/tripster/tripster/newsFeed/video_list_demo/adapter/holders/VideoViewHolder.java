package tripster.tripster.newsFeed.video_list_demo.adapter.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tripster.tripster.R;

public class VideoViewHolder extends RecyclerView.ViewHolder{

    public final tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView mPlayer;
    public final TextView mTitle;
    public final ImageView mCover;
    public final TextView mVisibilityPercents;

    public VideoViewHolder(View view) {
        super(view);
        mPlayer = (tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView) view.findViewById(R.id.player);
        mTitle = (TextView) view.findViewById(R.id.title);
        mCover = (ImageView) view.findViewById(R.id.cover);
        mVisibilityPercents = (TextView) view.findViewById(R.id.visibility_percents);
    }
}