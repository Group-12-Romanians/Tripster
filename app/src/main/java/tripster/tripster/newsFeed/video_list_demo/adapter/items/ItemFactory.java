package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.app.Activity;
import com.squareup.picasso.Picasso;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;

public class ItemFactory {

    public static DirectLinkVideoItem createItemFomDirectLink(String title,
                                                              String url,
                                                              String imageResource,
                                                              Activity activity,
                                                              VideoPlayerManager<MetaData> videoPlayerManager,
                                                              String friendsName,
                                                              String profilePictureUrl) {
        return new DirectLinkVideoItem(title,
                                       url,
                                       videoPlayerManager,
                                       Picasso.with(activity),
                                       imageResource,
                                       friendsName,
                                       profilePictureUrl);
    }
}

