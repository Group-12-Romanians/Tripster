package tripster.tripster.UILayer.trip.map;

import android.clustering.Cluster;
import android.clustering.ClusterItem;
import android.clustering.ClusterManager;
import android.clustering.view.DefaultClusterRenderer;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.ui.IconGenerator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.UILayer.trip.map.model.PhotoAtLocation;
import tripster.tripster.dataLayer.TripsterDb;

import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_TIME;
import static tripster.tripster.Constants.PHOTO_PATH_K;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;

public class MapActivity extends BaseDemoActivity implements
    ClusterManager.OnClusterClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterInfoWindowClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterItemClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterItemInfoWindowClickListener<PhotoAtLocation> {

  private static final String TAG = MapActivity.class.getName();
  private ClusterManager<PhotoAtLocation> clusterManager;
  private TripsterDb tDb;
  private LiveQuery imagesLQ;

  private String tripId;

  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  private GoogleApiClient client;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    tDb = TripsterDb.getInstance(getApplicationContext());
    tDb.initAllViews();
    tDb.startSync();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
  }

  /**
   * ATTENTION: This was auto-generated to implement the App Indexing API.
   * See https://g.co/AppIndexing/AndroidStudio for more information.
   */
  public Action getIndexApiAction() {
    Thing object = new Thing.Builder()
        .setName("Map Page") // TODO: Define a title for the content shown.
        // TODO: Make sure this auto-generated URL is correct.
        .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
        .build();
    return new Action.Builder(Action.TYPE_VIEW)
        .setObject(object)
        .setActionStatus(Action.STATUS_TYPE_COMPLETED)
        .build();
  }

  @Override
  public void onStart() {
    super.onStart();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    client.connect();
    AppIndex.AppIndexApi.start(client, getIndexApiAction());
  }

  @Override
  public void onStop() {
    super.onStop();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    AppIndex.AppIndexApi.end(client, getIndexApiAction());
    client.disconnect();
  }

  /**
   * Draws profile photos inside markers (using IconGenerator).
   * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
   */
  private class PhotoAtLocationRenderer extends DefaultClusterRenderer<PhotoAtLocation> {
    private final IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
    private final IconGenerator clusterIconGenerator = new IconGenerator(getApplicationContext());
    private final ImageView imageView;
    private final ImageView clusterImageView;
    private final int dimension;

    public PhotoAtLocationRenderer() {
      super(getApplicationContext(), getMap(), clusterManager);

      View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
      clusterIconGenerator.setContentView(multiProfile);
      clusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

      imageView = new ImageView(getApplicationContext());
      dimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
      imageView.setLayoutParams(new ViewGroup.LayoutParams(dimension, dimension));
      int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
      imageView.setPadding(padding, padding, padding, padding);
      iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(PhotoAtLocation photoAtLocation, MarkerOptions markerOptions) {
      super.onBeforeClusterItemRendered(photoAtLocation, markerOptions);
      markerOptions.title(photoAtLocation.getLocationName());
    }

    @Override
    protected void onClusterItemRendered(final PhotoAtLocation photo, Marker marker) {
      super.onClusterItemRendered(photo, marker);
      // Draw a single person.
      // Set the info window to show their name.
      Glide
          .with(getApplicationContext())
          .load(photo.getPhotoPath())
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
              imageView.setImageDrawable(resource);
              Bitmap icon = iconGenerator.makeIcon();
              Marker markerToChange = null;
              for (Marker marker : clusterManager.getMarkerCollection().getMarkers()) {
                if (marker.getPosition().equals(photo.getPosition())) {
                  markerToChange = marker;
                }
              }
              // if found - change icon
              if (markerToChange != null) {
                markerToChange.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
              }
            }
          });
      Bitmap icon = iconGenerator.makeIcon();
      marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterRendered(final Cluster<PhotoAtLocation> cluster, final Marker marker) {
      // Draw multiple people.
      // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
      final List<Drawable> photos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
      final int width = dimension;
      final int height = dimension;

      int i = 0;

      for (final PhotoAtLocation p : cluster.getItems()) {
        // Draw 4 at most.
        i++;
        Glide
            .with(getApplicationContext())
            .load(p.getPhotoPath())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(new SimpleTarget<GlideDrawable>() {
              @Override
              public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                resource.setBounds(0, 0, width, height);
                photos.add(resource);
                MultiDrawable multiDrawable = new MultiDrawable(photos);
                multiDrawable.setBounds(0, 0, width, height);

                clusterImageView.setImageDrawable(multiDrawable);
                Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
              }
            });

        if (i == 4) break;
      }
      Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
      marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
      // Always render clusters.
      return cluster.getSize() > 1;
    }
  }

  @Override
  public boolean onClusterClick(Cluster<PhotoAtLocation> cluster) {
    // Show a toast with some info when the cluster is clicked.
    String locationName = cluster.getItems().iterator().next().getLocationName();
    Toast.makeText(this, cluster.getSize() + " photos at " + locationName, Toast.LENGTH_SHORT).show();

    // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
    // inside of bounds, then animate to center of the bounds.

    // Create the builder to collect all essential cluster items for the bounds.
    LatLngBounds.Builder builder = LatLngBounds.builder();
    for (ClusterItem item : cluster.getItems()) {
      builder.include(item.getPosition());
    }
    // Get the LatLngBounds
    final LatLngBounds bounds = builder.build();

    // Animate camera to the bounds
    try {
      getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public void onClusterInfoWindowClick(Cluster<PhotoAtLocation> cluster) {
    // Does nothing, but you could go to a list of the users.
  }

  @Override
  public boolean onClusterItemClick(PhotoAtLocation item) {
    // Does nothing, but you could go into the user's profile page, for example.
    return false;
  }

  @Override
  public void onClusterItemInfoWindowClick(PhotoAtLocation item) {
    // Does nothing, but you could go into the user's profile page, for example.
  }

  @Override
  protected void startDemo() {
    Log.d(TAG, "Demo started");
    clusterManager = new ClusterManager<PhotoAtLocation>(this, getMap());
    clusterManager.setRenderer(new PhotoAtLocationRenderer());
    getMap().setOnCameraIdleListener((GoogleMap.OnCameraIdleListener) clusterManager);
    getMap().setOnMarkerClickListener(clusterManager);
    getMap().setOnInfoWindowClickListener(clusterManager);
    clusterManager.setOnClusterClickListener(this);
    clusterManager.setOnClusterInfoWindowClickListener(this);
    clusterManager.setOnClusterItemClickListener(this);
    clusterManager.setOnClusterItemInfoWindowClickListener(this);

    addItems();
    clusterManager.cluster();
  }

  private void addItems() {
    tripId = getIntent().getStringExtra("tripId");
    restartImagesLiveQuery();
  }

  private void restartImagesLiveQuery() {
    Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_TIME).createQuery();
    List<Object> firstKey = new ArrayList<>();
    firstKey.add(tripId);
    firstKey.add((long) 0);
    List<Object> lastKey = new ArrayList<>();
    lastKey.add(tripId);
    lastKey.add(System.currentTimeMillis());
    q.setStartKey(lastKey);
    q.setEndKey(firstKey);
    q.setDescending(true);
    imagesLQ = q.toLiveQuery();
    imagesLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final List<Pair<LatLng, List<String>>> results = new ArrayList<>();
        Iterator<QueryRow> it = event.getRows().iterator();
        String prevPlaceId = null;
        while (it.hasNext()) {
          QueryRow r = it.next();
          String placeId = (String) r.getValue();
          if (!placeId.equals(prevPlaceId)) {
            Document placeDoc =  tDb.getDocumentById(placeId);
            LatLng location = new LatLng((double) placeDoc.getProperty(PLACE_LAT_K), (double) placeDoc.getProperty(PLACE_LNG_K));
            results.add(new Pair<LatLng, List<String>>(location, new ArrayList<String>()));
            prevPlaceId = placeId;
          }
          results.get(results.size() - 1).second.add((String) r.getDocument().getProperty(PHOTO_PATH_K));
        }

        for (Pair<LatLng, List<String>> result : results) {
          for (String path : result.second) {
            clusterManager.addItem(new PhotoAtLocation("NoInfo", path, result.first));
          }
        }

      }
    });
    imagesLQ.start();
  }
}
