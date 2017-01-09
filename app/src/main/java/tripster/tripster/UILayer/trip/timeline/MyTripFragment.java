package tripster.tripster.UILayer.trip.timeline;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.github.channguyen.rsv.RangeSliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.Constants.LOCATIONS_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.SERVER_URL;
import static tripster.tripster.Constants.TRIP_LEVEL_K;
import static tripster.tripster.Constants.levels;
import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class MyTripFragment extends  TripFragment {

  private TextView levelHint;
  private LinearLayout levelInfo;
  private RangeSliderView levelSeek;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    assertNotNull(view);
    levelInfo = (LinearLayout) view.findViewById(R.id.levelInfo);
    levelHint = (TextView) view.findViewById(R.id.levelHint);
    levelSeek = (RangeSliderView) view.findViewById(R.id.levelSeek);
    updateLevelDetails();
    name.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Trip Name");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Map<String, Object> props = new HashMap<>();
            props.put(TRIP_NAME_K, input.getText().toString());
            tDb.upsertNewDocById(tripId, props);
          }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });

        builder.show();
        return false;
      }
    });

    final ImageButton optButton = (ImageButton) view.findViewById(R.id.options);
    optButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(getActivity(), optButton);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.trip_edit, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
              case R.id.redoPrev:
                redoPreview();
                break;
              case R.id.redoVideo:
                redoVideo();
                break;
              case R.id.deleteTrip:
                deleteTrip();
                break;
            }
            return true;
          }
        });

        popup.show();//showing popup menu
      }
    });

    description.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Trip Description");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Map<String, Object> props = new HashMap<>();
            props.put(TRIP_DESCRIPTION_K, input.getText().toString());
            tDb.upsertNewDocById(tripId, props);
          }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });

        builder.show();
        return false;
      }
    });
    return view;
  }

  private void updateLevelDetails() {
    Document d = tDb.getDocumentById(tripId);
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
  }

  private void deleteTrip() {
    getFragmentManager().popBackStack();
    try {
      // remove all places
      Query q = tDb.getDb().getExistingView(LOCATIONS_BY_TRIP_AND_TIME).createQuery();
      List<Object> firstKey = new ArrayList<>();
      firstKey.add(tripId);
      List<Object> lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(new HashMap<>());
      q.setStartKey(firstKey);
      q.setEndKey(lastKey);
      QueryEnumerator rows = q.run();
      for (int i = 0; i < rows.getCount(); i++) {
        rows.getRow(i).getDocument().delete();
      }

      // remove all photos
      q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_PLACE).createQuery();
      firstKey = new ArrayList<>();
      firstKey.add(tripId);
      lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(new HashMap<>());
      q.setStartKey(firstKey);
      q.setEndKey(lastKey);
      rows = q.run();
      for (int i = 0; i < rows.getCount(); i++) {
        rows.getRow(i).getDocument().delete();
      }

      // remove trip
      tDb.getDocumentById(tripId).delete();
    } catch (CouchbaseLiteException e) {
      e.printStackTrace();
    }
  }

  private void redoVideo() {
    RequestQueue queue = Volley.newRequestQueue(getContext());
    String url = SERVER_URL + "/updateVideo?tripId=" + tripId;
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
      }
    });
    queue.add(stringRequest);
  }

  private void redoPreview() {
    RequestQueue queue = Volley.newRequestQueue(getContext());
    String url = SERVER_URL + "/updatePreview?tripId=" + tripId;
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
      }
    });
    queue.add(stringRequest);
  }

  @Override
  protected int getFragmentLayout() {
    return R.layout.fragment_my_trip;
  }

  @Override
  protected void updateGeneralDetails() {
    super.updateGeneralDetails();
    if (description.getVisibility() == View.GONE) {
      description.setVisibility(View.VISIBLE);
      description.setText("Long touch to add a description for this trip");
    }
  }

  @Override
  protected void initListAdapter(List<String> events) {
    timeline.setAdapter(new MyTimelineAdapter(events));
  }
}
