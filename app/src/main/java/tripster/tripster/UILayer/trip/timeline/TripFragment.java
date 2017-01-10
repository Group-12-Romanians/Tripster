package tripster.tripster.UILayer.trip.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tripster.tripster.Constants;
import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TransactionManager;
import tripster.tripster.UILayer.trip.map.MapActivity;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.SHORT;
import static tripster.tripster.Constants.DEFAULT_NAME;
import static tripster.tripster.Constants.DEFAULT_PREVIEW;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.Constants.PLACES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_ID;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_STOPPED_AT_K;
import static tripster.tripster.Constants.TRIP_VIDEO_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class TripFragment extends Fragment {
  private static final String TAG = TripFragment.class.getName();

  String tripId;
  private LiveQuery placesLQ;

  private TransactionManager tM;

  RecyclerView timeline;

  TextView name;
  TextView description;
  TextView time;
  private ImageView preview;

  private Button videoBtn;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(getFragmentLayout(), container, false);
    tripId = this.getArguments().getString(TRIP_ID);
    timeline = (RecyclerView) view.findViewById(R.id.recyclerView);
    timeline.setLayoutManager(new LinearLayoutManager(getContext()));
    timeline.setHasFixedSize(true);

    videoBtn = (Button) view.findViewById(R.id.video);

    tM = new TransactionManager(getContext());

    view.findViewById(R.id.mapButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(TRIP_ID, tripId);
        getActivity().startActivity(intent);
      }
    });

    view.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_PLACE).createQuery();
        List<Object> firstKey = new ArrayList<>();
        firstKey.add(tripId);
        List<Object> lastKey = new ArrayList<>();
        lastKey.add(tripId);
        lastKey.add(new HashMap<>());
        q.setStartKey(firstKey);
        q.setEndKey(lastKey);
        try {
          QueryEnumerator rows = q.run();
          final List<Pair<Long, String>> results = new ArrayList<>();
          for (int i = 0; i < rows.getCount(); i++) {
            QueryRow r = rows.getRow(i);
            results.add(new Pair<>((long) r.getValue(), Constants.getPath(r.getDocumentId())));
          }
          Collections.sort(results, new Comparator<Pair<Long, String>>() {
            @Override
            public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
              return o1.first.compareTo(o2.first);
            }
          });
          ArrayList<String> photos = new ArrayList<>();
          String previewUrl = (String) tDb.getDocumentById(tripId).getProperty(TRIP_PREVIEW_K);
          if (previewUrl != null) {
            photos.add(previewUrl);
          }
          for (Pair<Long, String> result : results) {
            photos.add(result.second);
          }
          Log.d(TAG, "All Photos by time are: " + photos);
          ZGallery.with(getActivity(), photos)
              .setGalleryBackgroundColor(ZColor.WHITE) // activity background color
              .setToolbarColorResId(R.color.colorPrimary)// toolbar color
              .show();
        } catch (CouchbaseLiteException e) {
          Log.e(TAG, "Could not run images query.");
          e.printStackTrace();
        }
      }
    });

    name = (TextView) view.findViewById(R.id.tripName);
    description = (TextView) view.findViewById(R.id.tripDesc);
    preview = (ImageView) view.findViewById(R.id.preview);
    time = (TextView) view.findViewById(R.id.tripTime);

    return view;
  }

  protected int getFragmentLayout() {
    return R.layout.fragment_trip;
  }

  @Override
  public void onResume() {
    super.onResume();
    restartImportantPlacesLiveQuery();
    updateGeneralDetails();
    tDb.getDocumentById(tripId).addChangeListener(tripChangeListener);
  }

  private Document.ChangeListener tripChangeListener = new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          updateGeneralDetails();
        }
      });
    }
  };

  protected void updateGeneralDetails() {
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
      description.setVisibility(View.VISIBLE);
      description.setText(tripDesc);
    }

    // set trip preview and video link
    String tripPrev = (String) tripDoc.getProperty(TRIP_PREVIEW_K);
    if (tripPrev == null) {
      tripPrev = DEFAULT_PREVIEW;
    }
    new Image(tripPrev).displayIn(preview);
    final ArrayList<String> photos = new ArrayList<>();
    photos.add(tripPrev);
    preview.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ZGallery.with(getActivity(), photos)
            .setGalleryBackgroundColor(ZColor.WHITE) // activity background color
            .setToolbarColorResId(R.color.colorPrimary) // toolbar color
            .show();
      }
    });

    videoBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tM.accessVideo((String) tripDoc.getProperty(TRIP_VIDEO_K));
      }
    });

    Long t = (Long) tripDoc.getProperty(TRIP_STOPPED_AT_K);
    CharSequence timeText = "in progress";
    if (t != null) {
      timeText = DateUtils.formatSameDayTime(t, System.currentTimeMillis(), MEDIUM, SHORT);
    }
    time.setText(timeText);
  }

  private void restartImportantPlacesLiveQuery() {
    Query q = tDb.getDb().getExistingView(PLACES_BY_TRIP_AND_TIME).createQuery();
    List<Object> firstKey = new ArrayList<>();
    firstKey.add(tripId);
    firstKey.add((long) 0);
    List<Object> lastKey = new ArrayList<>();
    lastKey.add(tripId);
    lastKey.add(Long.MAX_VALUE);
    q.setStartKey(firstKey);
    q.setEndKey(lastKey);
    placesLQ = q.toLiveQuery();
    placesLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<String> results = new ArrayList<>();
        for (int i = 0; i < event.getRows().getCount(); i++) {
          results.add(event.getRows().getRow(i).getDocumentId());
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            initListAdapter(results);
          }
        });
      }
    });
    placesLQ.start();
  }

  @Override
  public void onPause() {
    try {
      tDb.getDocumentById(tripId).removeChangeListener(tripChangeListener);
      placesLQ.stop();
    } catch (NullPointerException e) {
    Log.e(TAG, "Something failed");
  }
    super.onPause();
  }

  protected void initListAdapter(List<String> events) {
    TimeLineAdapter timeLineAdapter = new TimeLineAdapter(events);
    int pos = ((LinearLayoutManager)timeline.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    timeline.setAdapter(timeLineAdapter);
    ((LinearLayoutManager)timeline.getLayoutManager()).scrollToPosition(pos);
  }
}
