package tripster.tripster.trips.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.trips.pictures.Photo;

public class PhotoTimelineTabFragment extends Fragment {

  private static final String TAG = PhotoTimelineTabFragment.class.getName();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_pictures, container, false);
    List<Event> events = this.getArguments().getParcelableArrayList("events");

    PhotosListAdapter adapter = new PhotosListAdapter(getActivity(), getPhotosFromEvents(events));
    ListView list = (ListView) view.findViewById(R.id.list);
    list.setAdapter(adapter);
    return view;
  }

  private List<Photo> getPhotosFromEvents(List<Event> events) {
    List<Photo> photos = new ArrayList<>();

    for (Event event : events) {
      for (String photoUri : event.getPhotoUris()) {
        photos.add(new Photo(photoUri, event.toString()));
      }
    }
    return photos;
  }
}
