package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;

public class ItemFactory {

    public static DirectLinkVideoItem createItemFomDirectLink(String title,
                                                              String url,
                                                              String imageResource,
                                                              FragmentActivity activity,
                                                              VideoPlayerManager<MetaData> videoPlayerManager,
                                                              String friendsName,
                                                              String profilePictureUrl,
                                                              String tripId) {
        return new DirectLinkVideoItem(title,
                                       url,
                                       videoPlayerManager,
                                       activity,
                                       imageResource,
                                       friendsName,
                                       profilePictureUrl, tripId);
    }
}

