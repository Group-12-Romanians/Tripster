package tripster.tripster.UILayer.trip.timeline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.couchbase.lite.CouchbaseLiteException;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;
import com.poliveira.apps.imagewindow.ImageWindow;

import java.util.ArrayList;

import tripster.tripster.Constants;
import tripster.tripster.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class MyTimeLineViewHolder extends TimeLineViewHolder {
  private static final String TAG = MyTimeLineViewHolder.class.getName();

  public MyTimeLineViewHolder(View view, int viewType) {
    super(view, viewType);
  }

  @Override
  View getView(View itemView, final String photoId, final ArrayList<String> photos, final int i) {
    String photoUri = Constants.getPath(photoId);

    final ImageWindow imageWindow = new ImageWindow(itemView.getContext());
    imageWindow.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    imageWindow.setCloseButtonSize(80);
    imageWindow.setOnCloseListener(new ImageWindow.OnCloseListener() {
      @Override
      public void onCloseClick(View view) {
        imageWindow.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            ((ViewGroup) (imageWindow.getParent())).removeView(imageWindow);
            try {
              tDb.getDocumentById(photoId).delete();
            } catch (CouchbaseLiteException e) {
              Log.e(TAG, "Could nto remove photo");
            }
          }
        }).start();
      }
    });

    final ImageView imageView = imageWindow.getImageView();
    imageView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ZGallery.with((AppCompatActivity) imageView.getContext(), photos)
            .setGalleryBackgroundColor(ZColor.WHITE) // activity background color
            .setToolbarColorResId(R.color.colorPrimary) // toolbar color
            .setSelectedImgPosition(i)
            .show();
      }
    });
    Glide.with(imageView.getContext())
        .load(photoUri)
        .override(600, 600)
        .fitCenter()
        .into(imageView);
    return imageWindow;
  }
}
