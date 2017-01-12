package tripster.tripster.UILayer.users.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.github.channguyen.rsv.RangeSliderView;

import java.util.HashMap;
import java.util.Map;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.FOL_LEVEL_K;
import static tripster.tripster.Constants.LEVEL_PUBLIC;
import static tripster.tripster.Constants.LEVEL_PUBLIC_DEFAULT;
import static tripster.tripster.Constants.levels;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class UserProfileFragment extends ProfileFragment {
  private static final String TAG = UserProfileFragment.class.getName();

  private String followerId;
  private String followingId;
  private int followerLevel = LEVEL_PUBLIC;

  private Button followerButton; // whether you follow the user or not

  private TextView userLevel;
  private LinearLayout userLevelInfo;
  private ImageButton userLevelBtn;

  private LinearLayout followingInfo;
  private TextView followingHint;
  private RangeSliderView followingSeek;

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
          props.put(FOL_LEVEL_K, LEVEL_PUBLIC_DEFAULT);
          props.put("_deleted", false);
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

    userLevelInfo = (LinearLayout) view.findViewById(R.id.user_level_info);
    userLevel = (TextView) view.findViewById(R.id.userLevel);
    userLevelBtn = (ImageButton) view.findViewById(R.id.userLevelMenu);

    followingInfo = (LinearLayout) view.findViewById(R.id.followingInfo);
    followingHint = (TextView) view.findViewById(R.id.levelHint);
    followingSeek = (RangeSliderView) view.findViewById(R.id.followSeek);
//    new Handler(Looper.getMainLooper()).post(new Runnable() {
//      @Override
//      public void run() {
//        followingInfo.setVisibility(View.GONE);
//      }
//    });

    updateUserLevelDetails();
    return view;
  }

  private void updateUserLevelDetails() {
    Document d = tDb.getDocumentById(followingId);
    if (d != null && !d.isDeleted()) {
      userLevelInfo.setVisibility(View.VISIBLE);
      int folLevel = (Integer) d.getProperty(FOL_LEVEL_K);
      userLevel.setText(levels.get(folLevel));
      userLevelBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (followingInfo.getVisibility() == View.GONE) {
            userLevelBtn.setImageResource(R.drawable.ic_expand_less_black_24dp);
            followingInfo.setVisibility(View.VISIBLE);
            activateFollowingDetails();
            Log.d(TAG, "Open");
          } else {
            userLevelBtn.setImageResource(R.drawable.ic_expand_more_black_24dp);
            followingInfo.setVisibility(View.GONE);
            Log.d(TAG, "Close");
          }
        }
      });
    } else {
      userLevelInfo.setVisibility(View.GONE);
      followingInfo.setVisibility(View.GONE);
    }
  }

  private void activateFollowingDetails() {
    Document d = tDb.getDocumentById(followingId);
    int folLevel = (Integer) d.getProperty(FOL_LEVEL_K);
    userLevel.setText(levels.get(folLevel));
    followingHint.setText("This user has visibility: " + levels.get(folLevel));
    if (folLevel == LEVEL_PUBLIC_DEFAULT) {
      followingSeek.setInitialIndex(LEVEL_PUBLIC);
    } else {
      followingSeek.setInitialIndex(folLevel);
    }
    followingSeek.setOnSlideListener(new RangeSliderView.OnSlideListener() {
      @Override
      public void onSlide(int index) {
        Document d = tDb.getDocumentById(followingId);
        if (d != null && !d.isDeleted()) {
          Map<String, Object> props = new HashMap<>();
          props.put(FOL_LEVEL_K, index);
          tDb.upsertNewDocById(followingId, props);
          activateFollowingDetails();
        }
      }
    });
  }

  @Override
  protected int getLayoutRes() {
    return R.layout.fragment_profile_user;
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
    int prevLevel = followerLevel;
    Document d = tDb.getDocumentById(followerId);
    if (d == null || d.isDeleted()) {
      followerLevel = LEVEL_PUBLIC;
      followerButton.setText("Follow");
    } else {
      followerLevel = Math.max((int) d.getProperty(FOL_LEVEL_K), LEVEL_PUBLIC);
      followerButton.setText("Unfollow");
    }
    if (followerLevel != prevLevel) {
      restartTripsLiveQuery();
    }
  }

  Document.ChangeListener followerChangeListener =  new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          updateFollowerDetails();
        }
      });
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
