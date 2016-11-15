package tripster.tripster.newsFeed.video_list_demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tripster.tripster.newsFeed.video_list_demo.adapter.holders.VideoViewHolder;
import tripster.tripster.newsFeed.video_list_demo.adapter.items.BaseVideoItem;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private final tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager mVideoPlayerManager;
    private final List<BaseVideoItem> mList;
    private final Context mContext;

    public VideoRecyclerViewAdapter(tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager videoPlayerManager, Context context, List<BaseVideoItem> list){
        mVideoPlayerManager = videoPlayerManager;
        mContext = context;
        mList = list;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        BaseVideoItem videoItem = mList.get(position);
        View resultView = videoItem.createView(viewGroup, mContext.getResources().getDisplayMetrics().widthPixels);
        return new VideoViewHolder(resultView);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder viewHolder, int position) {
        BaseVideoItem videoItem = mList.get(position);
        videoItem.update(position, viewHolder, mVideoPlayerManager);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
