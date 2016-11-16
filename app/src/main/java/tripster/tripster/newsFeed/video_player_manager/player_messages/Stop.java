package tripster.tripster.newsFeed.video_player_manager.player_messages;

import android.media.MediaPlayer;

import tripster.tripster.newsFeed.video_player_manager.PlayerMessageState;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManagerCallback;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

/**
 * This PlayerMessage calls {@link MediaPlayer#stop()} on the instance that is used inside {@link VideoPlayerView}
 */
public class Stop extends PlayerMessage {
    public Stop(VideoPlayerView videoView, VideoPlayerManagerCallback callback) {
        super(videoView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.stop();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.STOPPING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.STOPPED;
    }
}
