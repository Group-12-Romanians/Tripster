package tripster.tripster.UILayer.trip.timeline;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.R;

import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.Constants.LOCATIONS_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.PLACE_DESC_K;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;
import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.Constants.PLACE_TIME_K;
import static tripster.tripster.Constants.PLACE_TRIP_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class MyTimelineAdapter extends TimeLineAdapter {
  public MyTimelineAdapter(List<String> events) {
    super(events);
  }

  @Override
  public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = View.inflate(parent.getContext(), R.layout.item_my_timeline, null);
    return new MyTimeLineViewHolder(view, viewType);
  }

  @Override
  public void onBindViewHolder(final TimeLineViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    if (holder.descriptionTextView.getVisibility() == View.GONE) {
      holder.descriptionTextView.setVisibility(View.VISIBLE);
      holder.descriptionTextView.setText("Long touch to add a description for this place");
    }
    final String placeId = events.get(position);

    final ImageButton optButton = (ImageButton) holder.itemView.findViewById(R.id.options);
    optButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PopupMenu popup = new PopupMenu(optButton.getContext(), optButton);
        popup.getMenuInflater().inflate(R.menu.place_edit, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
              case R.id.addPlaceAfter:
                addPlaceAfter(placeId);
                break;
              case R.id.addPlaceBefore:
                addPlaceBefore(placeId);
                break;
              case R.id.deleteFullPlace:
                deleteFullPlace(placeId);
                break;
              case R.id.deletePlace:
                deletePlace(placeId);
                break;
            }
            return true;
          }
        });

        popup.show();//showing popup menu
      }
    });

    holder.descriptionTextView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Place Description");

        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText((CharSequence) tDb.getDocumentById(placeId).getProperty(PLACE_DESC_K));
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Map<String, Object> props = new HashMap<>();
            String desc = input.getText().toString();
            props.put(PLACE_DESC_K, desc);
            tDb.upsertNewDocById(placeId, props);
            holder.descriptionTextView.setText(desc);
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

    holder.nameTextView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Place Name");

        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText((CharSequence) tDb.getDocumentById(placeId).getProperty(PLACE_NAME_K));
        builder.setView(input);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Map<String, Object> props = new HashMap<>();
            String name = input.getText().toString();
            props.put(PLACE_NAME_K, name);
            tDb.upsertNewDocById(placeId, props);
            holder.nameTextView.setText(name);
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
  }

  private void deletePlace(String placeId) {
    try {
      Document doc = tDb.getDocumentById(placeId);

      // remove all images
      Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_PLACE).createQuery();
      List<Object> key = new ArrayList<>();
      key.add(doc.getProperty(PLACE_TRIP_K));
      key.add(placeId);
      q.setKeys(Collections.<Object>singletonList(key));
      QueryEnumerator rows = q.run();
      for (int i = 0; i < rows.getCount(); i++) {
        rows.getRow(i).getDocument().delete();
      }

      // remove place
      doc.delete();
    } catch (CouchbaseLiteException e) {
      e.printStackTrace();
    }
  }

  private void deleteFullPlace(String placeId) {
    try {
      // remove all locations before (excluding mine)
      Document doc = tDb.getDocumentById(placeId);
      String tripId = (String) doc.getProperty(PLACE_TRIP_K);
      long time = (long) doc.getProperty(PLACE_TIME_K);
      Query q = tDb.getDb().getExistingView(LOCATIONS_BY_TRIP_AND_TIME).createQuery();
      List<Object> firstKey = new ArrayList<>();
      firstKey.add(tripId);
      List<Object> lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(time + 1);
      q.setStartKey(firstKey);
      q.setEndKey(lastKey);
      QueryEnumerator rows = q.run();
      for (int i = rows.getCount() - 1; i >= 0; i--) {
        if (rows.getRow(i).getDocumentId().equals(placeId)) {
          continue;
        } else if (rows.getRow(i).getDocument().getProperty(PLACE_NAME_K) != null) {
          break;
        }
        rows.getRow(i).getDocument().delete();
      }

      // remove all locations after (excluding mine)
      q = tDb.getDb().getExistingView(LOCATIONS_BY_TRIP_AND_TIME).createQuery();
      firstKey = new ArrayList<>();
      firstKey.add(tripId);
      firstKey.add(time - 1);
      lastKey = new ArrayList<>();
      lastKey.add(tripId);
      lastKey.add(new HashMap<>());
      q.setStartKey(firstKey);
      q.setEndKey(lastKey);
      rows = q.run();
      for (int i = 0; i < rows.getCount(); i++) {
        if (rows.getRow(i).getDocumentId().equals(placeId)) {
          continue;
        } else if (rows.getRow(i).getDocument().getProperty(PLACE_NAME_K) != null) {
          break;
        }
        rows.getRow(i).getDocument().delete();
      }

      // delete place
      deletePlace(placeId);
    } catch (CouchbaseLiteException e) {
      e.printStackTrace();
    }
  }

  private void addPlaceAfter(String placeId) {
    Document doc = tDb.getDocumentById(placeId);
    Map<String, Object> newPlace= new HashMap<>();
    newPlace.put(PLACE_LAT_K, doc.getProperty(PLACE_LAT_K));
    newPlace.put(PLACE_LNG_K, doc.getProperty(PLACE_LNG_K));
    newPlace.put(PLACE_TIME_K, ((long) doc.getProperty(PLACE_TIME_K)) + 1);
    newPlace.put(PLACE_NAME_K, "On the road");
    newPlace.put(PLACE_DESC_K, "What have you been doing between these places?");
    newPlace.put(PLACE_TRIP_K, doc.getProperty(PLACE_TRIP_K));
    tDb.upsertNewDocById(UUID.randomUUID().toString(), newPlace);
  }

  private void addPlaceBefore(String placeId) {
    Document doc = tDb.getDocumentById(placeId);
    Map<String, Object> newPlace= new HashMap<>();
    newPlace.put(PLACE_LAT_K, doc.getProperty(PLACE_LAT_K));
    newPlace.put(PLACE_LNG_K, doc.getProperty(PLACE_LNG_K));
    newPlace.put(PLACE_TIME_K, ((long) doc.getProperty(PLACE_TIME_K)) - 1);
    newPlace.put(PLACE_NAME_K, "On the road");
    newPlace.put(PLACE_DESC_K, "What have you been doing between these places?");
    newPlace.put(PLACE_TRIP_K, doc.getProperty(PLACE_TRIP_K));
    tDb.upsertNewDocById(UUID.randomUUID().toString(), newPlace);
  }
}
