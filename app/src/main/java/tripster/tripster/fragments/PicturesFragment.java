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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.pictures.PhotosListAdapter;
import tripster.tripster.pictures.Picture;
import tripster.tripster.pictures.PicturesProvider;

public class PicturesFragment extends Fragment {

  private String TAG = PicturesFragment.class.getName();
  private static final String LOCATIONS_FILE_PATH = "locations.txt";
  private long startTime = -1;
  private PicturesProvider picturesProvider;
  static List<Picture> pictures;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_pictures, container, false);

    startTime = getTripStartTime();
    Log.d(TAG, getActivity().toString());

    picturesProvider = new PicturesProvider(getActivity(), startTime);
    if (startTime != -1) {
      pictures = getPicturesFromGallery();
      addPicturesToListView(pictures, view);
    } else {
      Log.d(TAG, "No time");
    }

    return view;
  }

  private long getTripStartTime() {
    long startTime = -1;
    try {
      File file = new File(getActivity().getFilesDir(), LOCATIONS_FILE_PATH);
      FileInputStream locationStream = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(locationStream));
      String line = reader.readLine();
      String firstTime = line.split(",")[0];
      startTime = Long.parseLong(firstTime);
      Log.d(TAG, "" + startTime);
    } catch (FileNotFoundException e) {
      Log.d(TAG, "No file to read from");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return startTime;
  }

  private void addPicturesToListView(List<Picture> pictures, View view) {
    int noOfPictures = pictures.size();
    String[] picturesDescriptions = new String[noOfPictures];
    Bitmap[] photos = new Bitmap[noOfPictures];
    Display display = getActivity().getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    Log.d("width of screen", "" + size.x + ", " + size.y);
    for (int i = 0; i < noOfPictures; i++) {
      Picture currentPicture = pictures.get(i);
      picturesDescriptions[i] = currentPicture.toString();
      photos[i] = currentPicture.getBitmap(size.x);
    }
    PhotosListAdapter adapter = new PhotosListAdapter(getActivity(), picturesDescriptions, photos);
    ListView list = (ListView) view.findViewById(R.id.list);
    list.setAdapter(adapter);
  }

  private List<Picture> getPicturesFromGallery() {
    return picturesProvider.getPhotos();
  }
}