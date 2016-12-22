package tripster.tripster.UILayer.trip.timeline;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.trip.map.MapActivity;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.Constants.TRIP_PREVIEW_K;
import static tripster.tripster.Constants.TRIP_VIDEO_K;
import static tripster.tripster.R.id.tripDesc;
import static tripster.tripster.R.id.tripName;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class TimelineFragment extends Fragment {
  private static final String TAG = TimelineFragment.class.getName();

  private String tripId;
  private LiveQuery imagesLQ;

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_timeline, container, false);
    tripId = this.getArguments().getString("tripId");
    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setHasFixedSize(true);

//  editButton = (Button) view.findViewById(R.id.editButton);
    Button locationButton = (Button) view.findViewById(R.id.noOfLocations);
    locationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("tripId", tripId);
        getActivity().startActivity(intent);
      }
    });
    return view;
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
    assertNotNull(getView());

    // set trip name
    ((TextView) getView().findViewById(tripName)).setText((String) tripDoc.getProperty(TRIP_NAME_K));

    // set trip description
    ((TextView) getView().findViewById(tripDesc)).setText((String) tripDoc.getProperty(TRIP_DESCRIPTION_K));

    // set trip preview and video link
    ImageView previewView = (ImageView) getView().findViewById(R.id.preview);
    new Image((String) tripDoc.getProperty(TRIP_PREVIEW_K)).displayIn(previewView);
    previewView.setOnClickListener(new View.OnClickListener() {
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
        List<Pair<String, List<String>>> results = new ArrayList<>();
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
        assertNotNull(getView());
        ((Button) getView().findViewById(R.id.noOfLocations)).setText(results.size());
        initListAdapter(results);
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

    assertNotNull(getView());
    ((RecyclerView) getView().findViewById(R.id.recyclerView)).setAdapter(timeLineAdapter);
  }
}
