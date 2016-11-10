package tripster.tripster.fragments;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import tripster.tripster.R;
import tripster.tripster.adapters.PhotosListAdapter;
import tripster.tripster.pictures.Picture;

public class PicturesFragment extends Fragment {

  private String TAG = PicturesFragment.class.getName();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_pictures, container, false);

    addPicturesToListView(HomeFragment.pictures, view);

    return view;
  }

  private void addPicturesToListView(List<Picture> pictures, View view) {
    int noOfPictures = pictures.size();
    String[] picturesDescriptions = new String[noOfPictures];
    Bitmap[] photos = new Bitmap[noOfPictures];

    Display display = getActivity().getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    Log.d(TAG, "width of screen: " + size.x + ", " + size.y);

    for (int i = 0; i < noOfPictures; i++) {
      Picture currentPicture = pictures.get(i);
      picturesDescriptions[i] = currentPicture.toString();
      photos[i] = currentPicture.getBitmap(size.x);
    }

    PhotosListAdapter adapter = new PhotosListAdapter(getActivity(), picturesDescriptions, photos);
    ListView list = (ListView) view.findViewById(R.id.list);
    list.setAdapter(adapter);
  }
}
