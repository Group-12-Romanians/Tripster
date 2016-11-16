package tripster.tripster.newsFeed.video_player_manager.manager;


import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;

public interface PlayerItemChangeListener {
    void onPlayerItemChanged(MetaData currentItemMetaData);
}
