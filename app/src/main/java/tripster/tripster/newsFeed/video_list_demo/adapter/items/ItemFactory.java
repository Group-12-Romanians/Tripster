package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.app.Activity;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ItemFactory {

    public static BaseVideoItem createItemFromAsset(String assetName, int imageResource,
                                                    Activity activity, tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager<tripster.tripster.newsFeed.video_player_manager.meta.MetaData> videoPlayerManager) throws IOException {
        return new AssetVideoItem(assetName, activity.getAssets().openFd(assetName),
                                    videoPlayerManager, Picasso.with(activity), imageResource);
    }
}

