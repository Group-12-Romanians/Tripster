package tripster.tripster.UILayer.users;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.FOL_LEVEL_K;
import static tripster.tripster.Constants.LEVEL_PUBLIC;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class UserProfileFragment extends ProfileFragment {
  private static final String TAG = UserProfileFragment.class.getName();

  private String followerId;
  private String followingId;
  private int followerLevel = LEVEL_PUBLIC;

  private Button followerButton; // wether you follow the user or not
  private SeekBar followingSeek; // wether he follows you and on what level

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    assertNotNull(view);
    followerId = currentUserId + ":" + userId;
    followingId = userId + ":" + currentUserId;

    followerButton = (Button) view.findViewById(R.id.followStatus);
    followerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Document d = tDb.getDocumentById(followerId);
        if (d == null || d.isDeleted()) {
          Map<String, Object> props = new HashMap<>();
          props.put(FOL_LEVEL_K, LEVEL_PUBLIC);
          tDb.upsertNewDocById(followerId, props);
          if (d == null) {
            restartFollowerListener();
          }
        } else {
          try {
            d.delete();
          } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Could not delete follower document");
          }
        }
      }
    });
    followingSeek = (SeekBar) view.findViewById(R.id.followSeek);
    Document d = tDb.getDocumentById(followingId);
    if (d != null && !d.isDeleted()) {
      followingSeek.setProgress((Integer) d.getProperty(FOL_LEVEL_K));
      followingSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          //TODO: catch changes in status and display them somewhere
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          Document d = tDb.getDocumentById(followingId);
          if (d != null && !d.isDeleted()) {
            Map<String, Object> props = new HashMap<>();
            props.put(FOL_LEVEL_K, seekBar.getProgress());
            tDb.upsertNewDocById(followingId, props);
          }
        }
      });
    } else {
      followingSeek.setVisibility(View.GONE);
    }

    return view;
  }

  @Override
  public void onResume() {
    restartFollowerListener();
    super.onResume();
  }

  private void restartFollowerListener() {
    updateFollowerDetails();
    Document d = tDb.getDocumentById(followerId);
    if (d != null) {
      d.addChangeListener(followerChangeListener);
    }
  }

  private void updateFollowerDetails() {
    followerButton.setVisibility(View.VISIBLE);
    Document d = tDb.getDocumentById(followerId);
    if (d == null || d.isDeleted()) {
      followerLevel = LEVEL_PUBLIC;
      followerButton.setText("Follow");
    } else {
      followerLevel = (int) d.getProperty(FOL_LEVEL_K);
      followerButton.setText("UnFollow");
    }
  }

  Document.ChangeListener followerChangeListener =  new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      int prevLevel = followerLevel;
      updateFollowerDetails();
      if (followerLevel != prevLevel) {
        restartTripsLiveQuery();
      }
    }
  };

  @Override
  public void onPause() {
    try {
      tDb.getDocumentById(followerId).removeChangeListener(followerChangeListener);
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }

  @Override
  public int getLevel() {
    return followerLevel;
  }
}
