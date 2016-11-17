package tripster.tripster.newsFeed.video_list_demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import tripster.tripster.newsFeed.video_list_demo.adapter.holders.VideoViewHolder;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.BaseVideoItem;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.DirectLinkVideoItem;
import tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager;

public class VideoListViewAdapter extends BaseAdapter {

    private final VideoPlayerManager mVideoPlayerManager;
    private final List<DirectLinkVideoItem> mList;
    private final Context mContext;

    public VideoListViewAdapter(VideoPlayerManager videoPlayerManager,
                                Context context,
                                List<DirectLinkVideoItem> list){
        mVideoPlayerManager = videoPlayerManager;
        mContext            = context;
        mList               = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseVideoItem videoItem = mList.get(position);

        View resultView;
        if(convertView == null){
            resultView = videoItem.createView(parent, mContext.getResources().getDisplayMetrics().widthPixels);
        } else {
            resultView = convertView;
        }

        try {
            videoItem.update(position, (VideoViewHolder) resultView.getTag(), mVideoPlayerManager);
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return resultView;
    }
}

