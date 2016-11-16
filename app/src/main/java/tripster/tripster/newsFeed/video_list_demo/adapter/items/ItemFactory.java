package tripster.tripster.newsFeed.video_list_demo.adapter.items;

import android.app.Activity;

import com.squareup.picasso.Picasso;

import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;
import tripster.tripster.newsFeed.video_player_manager.meta.MetaData;

public class ItemFactory {

//    public static BaseVideoItem createItemFromAsset(String assetName, int imageResource,
//                                                    Activity activity, VideoPlayerManager<MetaData> videoPlayerManager) throws IOException {
//        return new AssetVideoItem(assetName, activity.getAssets().openFd(assetName),
//                                    videoPlayerManager, Picasso.with(activity), imageResource);
//    }

    public static BaseVideoItem createItemFomDirectLink(
            String title, String url, int imageResource, Activity activity,
            VideoPlayerManager<MetaData> videoPlayerManager) {
        return new DirectLinkVideoItem(title, url, videoPlayerManager, Picasso.with(activity), imageResource);
    }
}

