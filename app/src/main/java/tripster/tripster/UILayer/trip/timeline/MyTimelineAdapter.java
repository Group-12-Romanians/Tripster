package tripster.tripster.UILayer.trip.timeline;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tripster.tripster.R;

import static tripster.tripster.Constants.PLACE_DESC_K;
import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class MyTimelineAdapter extends TimeLineAdapter {
  public MyTimelineAdapter(List<String> events) {
    super(events);
  }

  @Override
  public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = View.inflate(parent.getContext(), R.layout.item_timeline, null);
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
    holder.descriptionTextView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Place Description");

        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
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
}
