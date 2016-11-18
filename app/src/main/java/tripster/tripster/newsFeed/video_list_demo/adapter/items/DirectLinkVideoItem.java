package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.squareup.picasso.Picasso;

import tripster.tripster.R;
import tripster.tripster.newsFeed.video_list_demo.adapter.holders.VideoViewHolder;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;
import tripster.tripster.trips.tabs.HomeFragment;

/**
 * Use this class if you have direct path to the video source
 */
public class DirectLinkVideoItem extends BaseVideoItem {

    private final String mDirectUrl;
    private final String mTitle;
    private final String mFriendsName;
    private final Picasso mImageLoader;
    private final String mImageResource;

    private final String mProfilePictureUrl;
    private final String mTripId;
    private FragmentActivity activity;

    public DirectLinkVideoItem(String title, String directUr, VideoPlayerManager videoPlayerManager,
                               FragmentActivity activity, String imageResource, String friendsName,
                               String profilePictureUrl, String tripId) {
        super(videoPlayerManager);
        mDirectUrl         = directUr;
        mTitle             = title;
        mImageLoader       = Picasso.with(activity);
        mImageResource     = imageResource;
        mFriendsName       = friendsName;
        mProfilePictureUrl = profilePictureUrl;
        mTripId = tripId;
        this.activity = activity;
    }

    @Override
    public void update(int position,
                       VideoViewHolder viewHolder,
                       VideoPlayerManager videoPlayerManager){
        viewHolder.mTitle.setText(mTitle);
        viewHolder.mCover.setVisibility(View.VISIBLE);
        viewHolder.mCover.setTag(mTripId);
        mImageLoader.load(mImageResource).into(viewHolder.mCover);
        viewHolder.mCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripID = v.getTag().toString();
                if (!tripID.isEmpty()) {
                    accessTrip(v.getTag().toString());
                }
            }
        });
        viewHolder.mFriendsName.setText(mFriendsName);
        viewHolder.mProfilePicture.setVisibility(View.VISIBLE);
        mImageLoader.load(mProfilePictureUrl).fit().centerCrop().into(viewHolder.mProfilePicture);
    }

    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player,
                                                VideoPlayerManager<MetaData> videoPlayerManager) {
        videoPlayerManager.playNewVideo(currentItemMetaData, player, mDirectUrl);
    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager) {
        videoPlayerManager.stopAnyPlayback();
    }

    private void accessTrip(String tripId) {
        HomeFragment frag = new HomeFragment();
        Bundle arguments = new Bundle();
        arguments.putString("trip_id", tripId);
        frag.setArguments(arguments);
        FragmentTransaction trans = activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
        trans.addToBackStack("");
        trans.commit();
    }
}

