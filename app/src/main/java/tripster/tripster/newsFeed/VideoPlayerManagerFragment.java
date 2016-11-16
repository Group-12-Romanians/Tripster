package tripster.tripster.newsFeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tripster.tripster.R;
import tripster.tripster.newsFeed.video_player_manager.ui.SimpleMainThreadMediaPlayerListener;
import tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView;

public class VideoPlayerManagerFragment extends Fragment implements View.OnClickListener {

    private tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView mVideoPlayer_1;
    private tripster.tripster.newsFeed.video_player_manager.ui.VideoPlayerView mVideoPlayer_2;

    private tripster.tripster.newsFeed.video_player_manager.manager.VideoPlayerManager<tripster.tripster.newsFeed.video_player_manager.meta.MetaData> mVideoPlayerManager = new tripster.tripster.newsFeed.video_player_manager.manager.SingleVideoPlayerManager(new tripster.tripster.newsFeed.video_player_manager.manager.PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(tripster.tripster.newsFeed.video_player_manager.meta.MetaData metaData) {

        }
    });
//    private AssetFileDescriptor mVideoFileDecriptor_sample_1;
//    private AssetFileDescriptor mVideoFileDecriptor_sample_2;

    private ImageView mVideoCover;
    private ImageView mVideoCover2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.video_player_manager_fragment, container, false);

//        try {
//
//            mVideoFileDecriptor_sample_1 = getActivity().getAssets().openFd("video_sample_1.mp4");
//            mVideoFileDecriptor_sample_2 = getActivity().getAssets().openFd("video_sample_2.mp4");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mVideoPlayer_1 = (VideoPlayerView)root.findViewById(R.id.video_player_1);
        mVideoPlayer_1.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener(){
            @Override
            public void onVideoPreparedMainThread() {
                // We hide the cover when video is prepared. Playback is about to start
                mVideoCover.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideoStoppedMainThread() {
                // We show the cover when video is stopped
                mVideoCover.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideoCompletionMainThread() {
                // We show the cover when video is completed
                mVideoCover.setVisibility(View.VISIBLE);
            }
        });
        mVideoCover = (ImageView)root.findViewById(R.id.video_cover_1);
        mVideoCover.setOnClickListener(this);

        mVideoPlayer_2 = (VideoPlayerView)root.findViewById(R.id.video_player_2);
        mVideoPlayer_2.addMediaPlayerListener(new SimpleMainThreadMediaPlayerListener(){
            @Override
            public void onVideoPreparedMainThread() {
                // We hide the cover when video is prepared. Playback is about to start
                mVideoCover2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideoStoppedMainThread() {
                // We show the cover when video is stopped
                mVideoCover2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideoCompletionMainThread() {
                // We show the cover when video is completed
                mVideoCover2.setVisibility(View.VISIBLE);
            }
        });
        mVideoCover2 = (ImageView)root.findViewById(R.id.video_cover_2);
        mVideoCover2.setOnClickListener(this);

        Picasso.with(getActivity()).load(R.mipmap.video_sample_1_pic).into(mVideoCover);
        Picasso.with(getActivity()).load(R.mipmap.video_sample_2_pic).into(mVideoCover2);
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_cover_1:
                mVideoPlayerManager.playNewVideo(null, mVideoPlayer_1, "https://www.youtube.com/watch?v=GzKFEx-wsJo&ab_channel=Izabela%C5%9Awirkowska");
                break;
            case R.id.video_cover_2:
                mVideoPlayerManager.playNewVideo(null, mVideoPlayer_2, "https://www.youtube.com/watch?v=wqRuvmFfyag&ab_channel=RotonMusicTV");
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // in case we exited screen in playback
        mVideoCover.setVisibility(View.VISIBLE);
        mVideoCover2.setVisibility(View.VISIBLE);

        mVideoPlayerManager.stopAnyPlayback();
    }
}