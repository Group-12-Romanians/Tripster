package tripster.tripster.newsFeed.video_player_manager.player_messages;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.FileDescriptor;

import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManagerCallback;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

/**
 * This PlayerMessage calls {@link MediaPlayer#setDataSource(FileDescriptor)} on the instance that is used inside {@link VideoPlayerView}
 */
public class SetAssetsDataSourceMessage extends SetDataSourceMessage{

    //private final AssetFileDescriptor mAssetFileDescriptor;

    public SetAssetsDataSourceMessage(VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
        //mAssetFileDescriptor = assetFileDescriptor;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        //currentPlayer.setDataSource(mAssetFileDescriptor);
    }
}
