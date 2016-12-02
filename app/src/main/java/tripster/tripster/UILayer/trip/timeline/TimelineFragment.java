package tripster.tripster.UILayer.trip.timeline;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.TripsterActivity;
import tripster.tripster.UILayer.trip.timeline.events.ImageFromDoc;
import tripster.tripster.UILayer.trip.timeline.events.Place;
import tripster.tripster.UILayer.trip.timeline.events.Trip;
import tripster.tripster.dataLayer.TripsterDb;
import tripster.tripster.dataLayer.events.EditableTripEvent;
import tripster.tripster.dataLayer.events.ImagesChangedEvent;
import tripster.tripster.dataLayer.events.PlacesChangedEvent;
import tripster.tripster.dataLayer.events.TripsChangedEvent;

public class TimelineFragment extends Fragment {

  private static final String TAG = TimelineFragment.class.getName();
  private String tripId;
  private String userId;
  private Button editButton;
  private ListView timeline;

  private ImageView preview;
  private TextView tripName;
  private TextView tripDescription;

  private Set<String> importantPlaces;
  private Map<String, Document> places;
  private Map<String, List<Document>> images;
  private List<Pair<Document, List<Document>>> events;


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_timeline, container, false);
    // Get userId from bundle.
    tripId = this.getArguments().getString("tripId");
    userId = this.getArguments().getString("userId");
    timeline = (ListView) view.findViewById(R.id.events);
    preview = (ImageView) view.findViewById(R.id.preview);
    editButton = (Button) view.findViewById(R.id.editButton);
    if (!userId.equals(TripsterActivity.USER_ID)) {
      editButton.setVisibility(View.VISIBLE);
      editButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          changeToEditMode(tripId, events);
        }
      });
    }
    return view;
  }

  private void changeToEditMode(String tripId, List<Pair<Document, List<Document>>> events) {
    publishEditableTrip(tripId, events);
    // Change to the corresponding EditableFragment.
    EditableFragment frag = new EditableFragment();
    FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag);
    trans.addToBackStack("");
    trans.commit();
  }

  private void publishEditableTrip(String tripId, List<Pair<Document, List<Document>>> events) {
    List<Place> places = new ArrayList<>();
    List<ImageFromDoc> images = new ArrayList<>();

    for (Pair<Document, List<Document>> event : events) {
      places.add(new Place(event.first));
      for (Document image : event.second) {
        images.add(new ImageFromDoc(image));
      }
    }
    Document tripDoc = TripsterDb.getInstance().getHandle().getDocument(tripId);
    Trip trip = new Trip(tripDoc, places, images);
    EventBus.getDefault().postSticky(new EditableTripEvent(trip));
  }

  @Override
  public void onResume() {
    Log.d(TAG, "Register fragment");
    EventBus.getDefault().register(this);
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "UnRegister fragment");
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  private void initListAdapter() {
    events = new ArrayList<>();
    for (String importantPlace : importantPlaces) {
      if (places != null && places.containsKey(importantPlace)) {
        events.add(new Pair<>(places.get(importantPlace), images.get(importantPlace)));
      }
    }
    TimelineAdapter timelineAdapter = new TimelineAdapter(
        getActivity(),
        R.layout.event,
        R.id.locationName,
        events);
    timeline.setAdapter(timelineAdapter);
  }

  //-----------------------EVENTS--------------------------------------//
  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onPlaceChangedEvent(PlacesChangedEvent event) {
    Log.d(TAG, "In places change");
    places = new HashMap<>();
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    View view = getView();
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      Document placeDoc = row.getDocument();
      String placeTripId = (String) placeDoc.getProperty("tripId");
      // Get only the documents corresponding to the current user.
      if (tripId.equals(placeTripId)) {
        places.put(placeDoc.getId(), placeDoc);
      }
    }
    initListAdapter();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onImagesChangedEvent(ImagesChangedEvent event) {
    Log.d(TAG, "In images change");
    images = new HashMap<>();
    importantPlaces = new HashSet<>();
    int noOfImages = 0;
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    View view = getView();
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      Document imageDoc = row.getDocument();
      String imageTripId = (String) imageDoc.getProperty("tripId");
      String placeId = (String) imageDoc.getProperty("placeId");
      // Get only the documents corresponding to the current user.
      if (tripId.equals(imageTripId)) {
        noOfImages++;
        importantPlaces.add(placeId);
        List<Document> placeImages;
        if (!images.containsKey(placeId)) {
          placeImages = new ArrayList<>();
        } else {
          placeImages = images.get(placeId);
        }
        placeImages.add(imageDoc);
        images.put(placeId, placeImages);
      }
    }
    // Set the number of photos.
    ((Button) view.findViewById(R.id.noOfPhotos)).setText(String.valueOf(noOfImages));
    // Set the number of places.
    ((Button) view.findViewById(R.id.noOfLocations)).setText(String.valueOf(importantPlaces.size()));
    initListAdapter();
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onTripsChangedEvent(TripsChangedEvent event) {
    Log.d(TAG, "In trip change");
    LiveQuery.ChangeEvent liveChangeEvent = event.getEvent();
    QueryEnumerator changes = liveChangeEvent.getRows();
    for (int i = 0; i < changes.getCount(); i++) {
      QueryRow row = changes.getRow(i);
      Document tripDoc = row.getDocument();
      if (tripId.equals(tripDoc.getId())) {
        new Image((String) tripDoc.getProperty("preview"), "Description").displayIn(preview);

      }
    }
  }
}
