package tripster.tripster.newsFeed.video_list_demo.adapter.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tripster.tripster.R;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

public class VideoViewHolder extends RecyclerView.ViewHolder{

    public final VideoPlayerView mPlayer;
    public final TextView mTitle;
    public final ImageView mCover;
    public final TextView mFriendsName;
    public final ImageView mProfilePicture;


    public VideoViewHolder(View view) {
        super(view);
        mPlayer         = (VideoPlayerView) view.findViewById(R.id.player);
        mTitle          = (TextView) view.findViewById(R.id.title);
        mCover          = (ImageView) view.findViewById(R.id.cover);
        mFriendsName    = (TextView) view.findViewById(R.id.friend_name);
        mProfilePicture = (ImageView) view.findViewById(R.id.profile_picture);

    }
}