package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.view.View;

import com.squareup.picasso.Picasso;

import tripster.tripster.newsFeed.video_list_demo.adapter.holders.VideoViewHolder;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

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

    public DirectLinkVideoItem(String title, String directUr, VideoPlayerManager videoPlayerManager,
                               Picasso imageLoader, String imageResource, String friendsName,
                               String profilePictureUrl) {
        super(videoPlayerManager);
        mDirectUrl         = directUr;
        mTitle             = title;
        mImageLoader       = imageLoader;
        mImageResource     = imageResource;
        mFriendsName       = friendsName;
        mProfilePictureUrl = profilePictureUrl;
    }

    @Override
    public void update(int position,
                       VideoViewHolder viewHolder,
                       VideoPlayerManager videoPlayerManager){
        viewHolder.mTitle.setText(mTitle);
        viewHolder.mCover.setVisibility(View.VISIBLE);
        mImageLoader.load(mImageResource).into(viewHolder.mCover);
//        viewHolder.mFriendsName.setText(mFriendsName);
//        viewHolder.mProfilePicture.setVisibility(View.VISIBLE);
//        Uri uri = Uri.parse(mProfilePictureUrl);
//        viewHolder.mProfilePicture.setImageURI(uri);
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
}

