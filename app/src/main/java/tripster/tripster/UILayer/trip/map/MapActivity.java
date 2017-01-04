package tripster.tripster.UILayer.trip.map;

import android.clustering.Cluster;
import android.clustering.ClusterItem;
import android.clustering.ClusterManager;
import android.clustering.view.DefaultClusterRenderer;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.ui.IconGenerator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import tripster.tripster.Constants;
import tripster.tripster.R;
import tripster.tripster.UILayer.trip.map.model.PhotoAtLocation;
import tripster.tripster.dataLayer.TripsterDb;

import static tripster.tripster.Constants.IMAGES_BY_TRIP_AND_PLACE;
import static tripster.tripster.Constants.LOCATIONS_BY_TRIP;
import static tripster.tripster.Constants.PLACE_LAT_K;
import static tripster.tripster.Constants.PLACE_LNG_K;
import static tripster.tripster.Constants.PLACE_NAME_K;
import static tripster.tripster.Constants.TRIP_ID;

public class MapActivity extends BaseDemoActivity implements
    ClusterManager.OnClusterClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterInfoWindowClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterItemClickListener<PhotoAtLocation>,
    ClusterManager.OnClusterItemInfoWindowClickListener<PhotoAtLocation> {

  private static final String TAG = MapActivity.class.getName();
  private ClusterManager<PhotoAtLocation> clusterManager;
  private TripsterDb tDb;

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
    protected void onBeforeClusterItemRendered(final PhotoAtLocation photoAtLocation, MarkerOptions markerOptions) {
      imageView.setImageResource(R.drawable.amu_bubble_mask); //temp image.
      Bitmap icon = iconGenerator.makeIcon();
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
      markerOptions.title(photoAtLocation.getLocationName());
    }

    @Override
    protected void onClusterItemRendered(final PhotoAtLocation photo, final Marker marker) {
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
              marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            }
          });
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



  private ArrayList<LatLng> points; //added
  private Polyline line; //added

  private LiveQuery locationsLQ;

  @Override
  protected void startDemo() {
    Log.d(TAG, "Demo started");
    tripId = getIntent().getStringExtra(TRIP_ID);


    clusterManager = new ClusterManager<PhotoAtLocation>(this, getMap());
    clusterManager.setRenderer(new PhotoAtLocationRenderer());
    getMap().setOnCameraIdleListener((GoogleMap.OnCameraIdleListener) clusterManager);
    getMap().setOnMarkerClickListener(clusterManager);
    getMap().setOnInfoWindowClickListener(clusterManager);
    clusterManager.setOnClusterClickListener(this);
    clusterManager.setOnClusterInfoWindowClickListener(this);
    clusterManager.setOnClusterItemClickListener(this);
    clusterManager.setOnClusterItemInfoWindowClickListener(this);

    addImages();
    clusterManager.cluster();

    addPolyLine();
  }

  private void addPolyLine() {
    startPlacesLiveQuery();
  }

  private void startPlacesLiveQuery() {
    Query q = tDb.getDb().getExistingView(LOCATIONS_BY_TRIP).createQuery();
    q.setKeys(Collections.<Object>singletonList(tripId));
    locationsLQ = q.toLiveQuery();
    locationsLQ.addChangeListener(new LiveQuery.ChangeListener() {
      @Override
      public void changed(LiveQuery.ChangeEvent event) {
        final PolylineOptions options = new PolylineOptions().width(4).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < event.getRows().getCount(); i++) {
          QueryRow r = event.getRows().getRow(i);
          Document locDoc = r.getDocument();
          LatLng point = new LatLng((double) locDoc.getProperty(PLACE_LAT_K), (double) locDoc.getProperty(PLACE_LNG_K));
          options.add(point);
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            if (line != null) {
              line.remove();
            }
            line = getMap().addPolyline(options); //add Polyline
          }
        });
      }
    });
    locationsLQ.start();
  }

  private void addImages() {
    startImagesQuery();
  }

  private void startImagesQuery() {
    Query q = tDb.getDb().getExistingView(IMAGES_BY_TRIP_AND_PLACE).createQuery();
    List<Object> firstKey = new ArrayList<>();
    firstKey.add(tripId);
    List<Object> lastKey = new ArrayList<>();
    lastKey.add(tripId);
    lastKey.add(new HashMap<>());
    q.setStartKey(firstKey);
    q.setEndKey(lastKey);
    try {
      QueryEnumerator rows = q.run();
      for (int i = 0; i < rows.getCount(); i++) {
        QueryRow r = rows.getRow(i);
        String placeId = ((List<String>) r.getKey()).get(1); // get the placeId from the key
        Document placeDoc =  tDb.getDocumentById(placeId);
        LatLng location = new LatLng((double) placeDoc.getProperty(PLACE_LAT_K), (double) placeDoc.getProperty(PLACE_LNG_K));
        clusterManager.addItem(new PhotoAtLocation((String) placeDoc.getProperty(PLACE_NAME_K), Constants.getPath(r.getDocumentId()), location));
      }
    } catch (CouchbaseLiteException e) {
      Log.e(TAG, "Cannot run images live query.");
      e.printStackTrace();
    }
  }
}
