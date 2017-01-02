package tripster.tripster.UILayer.trip.timeline;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.github.channguyen.rsv.RangeSliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.trip.map.MapActivity;

import static tripster.tripster.Constants.DEFAULT_NAME;
import static tripster.tripster.Constants.DEFAULT_PREVIEW;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_ID;
import static tripster.tripster.Constants.TRIP_LEVEL_K;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_OWNER_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_VIDEO_K;
import static tripster.tripster.Constants.levels;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class TimelineFragment extends Fragment {
  private static final String TAG = TimelineFragment.class.getName();

  private String tripId;
  private LiveQuery imagesLQ;

  private RecyclerView timeline;

  private Button editButton;
  private Button locationButton;
  private TextView name;
  private TextView description;
  private ImageView preview;

  private TextView levelHint;
  private LinearLayout levelInfo;
  private RangeSliderView levelSeek;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_timeline, container, false);
    tripId = this.getArguments().getString(TRIP_ID);
    timeline = (RecyclerView) view.findViewById(R.id.recyclerView);
    timeline.setLayoutManager(new LinearLayoutManager(getContext()));
    timeline.setHasFixedSize(true);

    locationButton = (Button) view.findViewById(R.id.noOfLocations);
    name = (TextView) view.findViewById(R.id.tripName);
    description = (TextView) view.findViewById(R.id.tripDesc);
    preview = (ImageView) view.findViewById(R.id.preview);

    locationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(TRIP_ID, tripId);
        getActivity().startActivity(intent);
      }
    });

    levelInfo = (LinearLayout) view.findViewById(R.id.levelInfo);
    levelHint = (TextView) view.findViewById(R.id.levelHint);
    levelSeek = (RangeSliderView) view.findViewById(R.id.levelSeek);
    updateLevelDetails();

    return view;
  }

  private void updateLevelDetails() {
    Document d = tDb.getDocumentById(tripId);
    if (d.getProperty(TRIP_OWNER_K).equals(currentUserId)) {
      int level = (Integer) d.getProperty(TRIP_LEVEL_K);
      levelHint.setText("This trip has visibility: " + levels.get(level));
      levelSeek.setInitialIndex(level);
      levelSeek.setOnSlideListener(new RangeSliderView.OnSlideListener() {
        @Override
        public void onSlide(int index) {
          Map<String, Object> props = new HashMap<>();
          props.put(TRIP_LEVEL_K, index);
          tDb.upsertNewDocById(tripId, props);
          updateLevelDetails();
        }
      });
    } else {
      levelInfo.setVisibility(View.GONE);
    }

  }

  @Override
  public void onResume() {
    super.onResume();
    restartImagesLiveQuery();
    setGeneralDetails();
  }

  private void setGeneralDetails() {
    final Document tripDoc = tDb.getDocumentById(tripId);
    Log.d(TAG, "Current trip changed, id is:" + tripDoc.getId());

    // set trip name
    String tripName = (String) tripDoc.getProperty(TRIP_NAME_K);
    if (tripName == null) {
      tripName = DEFAULT_NAME;
    }
    name.setText(tripName);

    // set trip description
    String tripDesc = (String) tripDoc.getProperty(TRIP_DESCRIPTION_K);
    if (tripDesc == null) {
      description.setVisibility(View.GONE);
    } else {
      description.setText(tripDesc);
    }

    // set trip preview and video link
    String tripPrev = (String) tripDoc.getProperty(TRIP_PREVIEW_K);
    if (tripPrev == null) {
      tripPrev = DEFAULT_PREVIEW;
    }
    new Image(tripPrev).displayIn(preview);
    preview.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String videoUrl = (String) tripDoc.getProperty(TRIP_VIDEO_K);
        Log.d(TAG, "Video url is " + videoUrl );
        if (videoUrl != null) {
          intent.setDataAndType(Uri.parse(videoUrl), "video/*");
          if (getContext() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getContext();
            activity.startActivity(Intent.createChooser(intent, "Complete action using"));
          }
        }
      }
    });

  }

  private void restartImagesLiveQuery() {
    Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_TIME).createQuery();
    List<Object> firstKey = new ArrayList<>();
    firstKey.add(tripId);
    firstKey.add((long) 0);
    List<Object> lastKey = new ArrayList<>();
    lastKey.add(tripId);
    lastKey.add(System.currentTimeMillis());
    q.setStartKey(lastKey);
    q.setEndKey(firstKey);
    q.setDescending(true);
    imagesLQ = q.toLiveQuery();
    imagesLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<Pair<String, List<String>>> results = new ArrayList<>();
        Iterator<QueryRow> it = event.getRows().iterator();
        String prevPlaceId = null;
        while (it.hasNext()) {
          QueryRow r = it.next();
          String placeId = (String) r.getValue();
          if (!placeId.equals(prevPlaceId)) {
            results.add(new Pair<String, List<String>>(placeId, new ArrayList<String>()));
            prevPlaceId = placeId;
          }
          results.get(results.size() - 1).second.add(r.getDocumentId());
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            locationButton.setText(String.valueOf(results.size()));
            initListAdapter(results);
          }
        });
      }
    });
    imagesLQ.start();
  }

  @Override
  public void onPause() {
    try {
      imagesLQ.stop();
    } catch (NullPointerException e) {
    Log.e(TAG, "Something failed");
  }
    super.onPause();
  }

  private void initListAdapter(List<Pair<String, List<String>>> events) {
    TimeLineAdapter timeLineAdapter = new TimeLineAdapter(events);
    timeline.setAdapter(timeLineAdapter);
  }
}
