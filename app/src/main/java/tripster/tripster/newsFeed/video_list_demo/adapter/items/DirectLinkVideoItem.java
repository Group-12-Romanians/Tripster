package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.view.View;

import com.squareup.picasso.Picasso;

import tripster.tripster.newsFeed.video_list_demo.adapter.holders.VideoViewHolder;

/**
 * Use this class if you have direct path to the video source
 */
public class DirectLinkVideoItem extends BaseVideoItem {

    private final String mDirectUrl;
    private final String mTitle;

    private final Picasso mImageLoader;
    private final int mImageResource;

    public DirectLinkVideoItem(String title, String directUr, tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager videoPlayerManager, Picasso imageLoader, int imageResource) {
        super(videoPlayerManager);
        mDirectUrl = directUr;
        mTitle = title;
        mImageLoader = imageLoader;
        mImageResource = imageResource;

    }

    @Override
    public void update(int position, VideoViewHolder viewHolder, tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager videoPlayerManager) {
        viewHolder.mTitle.setText(mTitle);
        viewHolder.mCover.setVisibility(View.VISIBLE);
        mImageLoader.load(mImageResource).into(viewHolder.mCover);
    }

    @Override
    public void playNewVideo(tripster.tripster.newsFeed.video_player_manager.meta.MetaData currentItemMetaData, tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView player, tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager<tripster.tripster.newsFeed.video_player_manager.meta.MetaData> videoPlayerManager) {
        videoPlayerManager.playNewVideo(currentItemMetaData, player, mDirectUrl);
    }

    @Override
    public void stopPlayback(tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager videoPlayerManager) {
        videoPlayerManager.stopAnyPlayback();
    }
}

