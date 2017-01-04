package tripster.tripster.UILayer.trip.timeline;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tripster.tripster.Constants.TRIP_DESCRIPTION_K;
import static tripster.tripster.Constants.TRIP_NAME_K;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class MyTripFragment extends  TripFragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
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
