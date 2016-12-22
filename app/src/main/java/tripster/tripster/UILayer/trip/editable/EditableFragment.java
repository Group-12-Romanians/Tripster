package tripster.tripster.UILayer.trip.editable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.dataLayer.events.EditableTripEvent;

public class EditableFragment extends Fragment {
  private Trip trip;
  private ImagesGridAdapter imagesAdapter;
  private PlaceListAdapter placesAdapter;
  ListView placesView;
  GridView imagesView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_ediatble, container, false);
    placesView = (ListView) view.findViewById(R.id.locations);
    imagesView = (GridView) view.findViewById(R.id.images);
    return view;
  }

  @Override
  public void onResume() {
    EventBus.getDefault().register(this);
    super.onResume();
  }

  //-----------------------------EVENTS--------------------------------------//
  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onEditableTripEvent(EditableTripEvent event) {
    Trip trip = event.getTrip();
    View view = getView();
    ((EditText) view.findViewById(R.id.tripName)).setText(trip.getName());
    ((EditText) view.findViewById(R.id.tripDescription)).setText(trip.getDescription());
    initPlaceListView(trip.getPlaces());
    initImagesGridView(trip.getImages());
    EventBus.getDefault().unregister(this);
  }

  private void initImagesGridView(List<ImageFromDoc> images) {
    imagesAdapter = new ImagesGridAdapter(
        getContext(),
        R.layout.editable_image,
        R.id.tripDescription,
        images);
    imagesView.setAdapter(imagesAdapter);
  }

  private void initPlaceListView(List<Place> places) {
    placesAdapter = new PlaceListAdapter(
        getContext(),
        R.layout.editable_place,
        R.id.tripDescription,
        places
    );
    placesView.setAdapter(placesAdapter);
  }


}
