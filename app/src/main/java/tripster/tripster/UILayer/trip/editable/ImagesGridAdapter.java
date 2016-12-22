package tripster.tripster.UILayer.trip.editable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import tripster.tripster.Image;
import tripster.tripster.R;

public class ImagesGridAdapter extends ArrayAdapter {
  private List<ImageFromDoc> images;
  private Context context;

  public ImagesGridAdapter(Context context, int resource, int textViewResourceId, List<ImageFromDoc> images) {
    super(context, resource, textViewResourceId, images);
    this.images = images;
    this.context = context;
  }

  public class ViewHolderEditableImagePreview {
    // Location details
    ImageView image;
    EditText placeDescription;
  }

  @Override
  public int getCount() {
    return images.size();
  }

  @Override
  public Object getItem(int position) {
    return images.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @NonNull
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater view
          = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = view.inflate(R.layout.editable_image, null);
      ViewHolderEditableImagePreview holder = new ViewHolderEditableImagePreview();
      // Set location details
      holder.placeDescription = (EditText) convertView.findViewById(R.id.description);
      holder.image = (ImageView) convertView.findViewById(R.id.image);
      convertView.setTag(holder);
    }

    EditText imageDescriptionView = ((ViewHolderEditableImagePreview) convertView.getTag()).placeDescription;
    imageDescriptionView.setText(images.get(position).getDescription());

    ImageView imageView = ((ViewHolderEditableImagePreview) convertView.getTag()).image;
    new Image(images.get(position).getPath()).displayIn(imageView);
    return convertView;
  }
}
