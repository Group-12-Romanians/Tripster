package tripster.tripster.newsFeed.video_player_manager.player_messages;

import tripster.tripster.newsFeed.video_player_manager.PlayerMessageState;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManagerCallback;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

/**
 * This PlayerMessage creates new MediaPlayer instance that will be used inside {@link VideoPlayerView}
 */
public class CreateNewPlayerInstance extends PlayerMessage {

    public CreateNewPlayerInstance(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.createNewPlayerInstance();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.CREATING_PLAYER_INSTANCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.PLAYER_INSTANCE_CREATED;
    }
}
