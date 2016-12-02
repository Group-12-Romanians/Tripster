package tripster.tripster.UILayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.github.clans.fab.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.newsfeed.NewsfeedFragment;
import tripster.tripster.UILayer.notifications.NotificationsFragment;
import tripster.tripster.UILayer.users.SearchForUsersFragment;
import tripster.tripster.UILayer.users.UserProfileFragment;
import tripster.tripster.account.LogoutProvider;
import tripster.tripster.dataLayer.TripsterDb;
import tripster.tripster.dataLayer.events.FriendsChangedEvent;
import tripster.tripster.dataLayer.events.PlacesChangedEvent;
import tripster.tripster.dataLayer.events.TripsChangedEvent;
import tripster.tripster.dataLayer.events.UsersChangedEvent;
import tripster.tripster.services.LocationService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TripsterActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  public static final String SERVER_URL = "http://146.169.46.220:8081";
  private static final String TAG = TripsterActivity.class.getName();
  private static final int MY_PERMISSIONS_REQUEST = 47;

  public static RequestQueue reqQ;

  public static String USER_ID = "";

  private LogoutProvider accountProvider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tripster);
    Fragment frag = new NewsfeedFragment();
    getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();

    askForPermissions();
    initializeDrawer();
    reqQ = Volley.newRequestQueue(this);
  }

  @Override
  protected void onStart() {
    USER_ID = getIntent().getStringExtra("id");
    getSharedPreferences("UUID", MODE_PRIVATE).edit().putString("UUID", USER_ID).apply();
    Log.d(TAG, "MeUser ID is: " + USER_ID);
    TripsterDb.getInstance(getApplicationContext()).startSync();

    recreateLoginSession();

    initializeFab();

    EventBus.getDefault().register(this);
    TripsterDb.getInstance().startUsersLiveQuery();
    TripsterDb.getInstance().startPlacesLiveQuery();
    TripsterDb.getInstance().startTripsLiveQuery();
    TripsterDb.getInstance().startFriendsLiveQuery();
    TripsterDb.getInstance().startImagesLiveQuery();

    //Update or Create the current User
    Map<String, Object> props = new HashMap<>();
    props.put("name", getIntent().getStringExtra("name"));
    props.put("email", getIntent().getStringExtra("email"));
    props.put("avatarUrl", getIntent().getStringExtra("avatarUrl"));
    TripsterDb.getInstance().upsertNewDocById(USER_ID, props);

    super.onStart();
  }

  @Override
  protected void onStop() {
    EventBus.getDefault().unregister(this);
    TripsterDb.close();
    super.onStop();
  }

  private void askForPermissions() {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(this,
          new String[]{ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String perms[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST: {
        if (grantResults.length != 2 || grantResults[0] == PERMISSION_DENIED ||  grantResults[1] == PERMISSION_DENIED) {
          Toast.makeText(this, "Cannot proceed without your permission!", Toast.LENGTH_LONG).show();
          accountProvider.logOut();
        }
      }
    }
  }

  private void initializeDrawer() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
  }

  private void initializeFab() {
    final FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.tracking_start);
    final FloatingActionButton pauseButton = (FloatingActionButton) findViewById(R.id.tracking_pause);
    final FloatingActionButton resumeButton = (FloatingActionButton) findViewById(R.id.tracking_resume);
    final FloatingActionButton endButton = (FloatingActionButton) findViewById(R.id.tracking_stop);

    switch (getServiceStatus()) {
      case "running":
        // show pause and end
        startButton.hideButtonInMenu(true);
        resumeButton.hideButtonInMenu(true);
        break;
      case "stopped":
        // show start
        pauseButton.hideButtonInMenu(true);
        resumeButton.hideButtonInMenu(true);
        endButton.hideButtonInMenu(true);
        break;
      case "paused":
        // show resume and end
        startButton.hideButtonInMenu(true);
        pauseButton.hideButtonInMenu(true);
        break;
    }

    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to start recording");
        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
        serviceIntent.putExtra("flag", "start");
        serviceIntent.putExtra("user_id", USER_ID);
        TripsterActivity.this.startService(serviceIntent);

        startButton.hideButtonInMenu(true);
        pauseButton.showButtonInMenu(true);
        endButton.showButtonInMenu(true);
      }
    });

    pauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to pause recording");
        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
        serviceIntent.putExtra("flag", "pause");
        serviceIntent.putExtra("user_id", USER_ID);
        TripsterActivity.this.startService(serviceIntent);

        pauseButton.hideButtonInMenu(true);
        resumeButton.showButtonInMenu(true);
      }
    });

    resumeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to pause recording");
        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
        serviceIntent.putExtra("flag", "resume");
        serviceIntent.putExtra("user_id", USER_ID);
        TripsterActivity.this.startService(serviceIntent);

        resumeButton.hideButtonInMenu(true);
        pauseButton.showButtonInMenu(true);
      }
    });

    endButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to stop recording");
        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
        serviceIntent.putExtra("flag", "stop");
        serviceIntent.putExtra("user_id", USER_ID);
        TripsterActivity.this.startService(serviceIntent);

        resumeButton.hideButtonInMenu(true);
        pauseButton.hideButtonInMenu(true);
        endButton.hideButtonInMenu(true);
        startButton.showButtonInMenu(true);
      }
    });
  }

  private String getServiceStatus() {
    Document currTrip = TripsterDb.getInstance().getCurrentlyRunningTrip(USER_ID);
    if (currTrip == null) {
      return "stopped";
    } else {
      if (currTrip.getProperty("status").equals("running")) {
        Log.d(TAG, "Running");
        return "running";
      } else {
        Log.d(TAG, "Paused");
        return "paused";
      }
    }
  }

  private void recreateLoginSession() {
    String loginProviderClassName = getIntent().getStringExtra("loginProvider");
    Log.d(TAG, "MeUser logged in with: " + loginProviderClassName);
    try {
      Class<?> loginProviderClass = Class.forName(loginProviderClassName);
      Constructor<?> cons = loginProviderClass.getConstructor(AppCompatActivity.class);
      Object obj = cons.newInstance(this);
      accountProvider = (LogoutProvider) obj;
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    Fragment frag = null;
    if (id == R.id.nav_camera) {
      frag = new UserProfileFragment();
      Bundle args = new Bundle();
      args.putString("userId", USER_ID);
      frag.setArguments(args);
      Log.d(TAG, "I want to switch to myTrips fragment");
    } else if (id == R.id.nav_slideshow) {
      frag = new SearchForUsersFragment();
      Bundle args = new Bundle();
      args.putString("userId", "none");
      frag.setArguments(args);
      Log.d(TAG, "I want to switch to allUsers fragment");
    } else if (id == R.id.news_feed) {
      Log.d(TAG, "I want to switch to newsFeed fragment");
      frag = new NewsfeedFragment();
    } else if (id == R.id.nav_send) {
      frag = new NotificationsFragment();
      Log.d(TAG, "I want to switch to notifications fragment");

    } else if (id == R.id.nav_logout) {
      Log.d(TAG, "I want to switch to logout");
      accountProvider.logOut();
      return true;
    }

    if (frag != null) {
      getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  //----------------------------------------- EVENTS ---------------------------------------------//

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onUsersChangedEvent(UsersChangedEvent event) {
    QueryEnumerator enumerator = event.getEvent().getRows();
    View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
    for (int i = 0; i < enumerator.getCount(); i++) {
      QueryRow row = enumerator.getRow(i);
      if (row.getDocument().getId().equals(USER_ID)) {
        Log.d(TAG, "Update user me: " + row.getDocument().getId());
        Document me = row.getDocument();
        ((TextView) header.findViewById(R.id.username)).setText((String) me.getProperty("name"));
        Log.d(TAG, "MEMMEME" + (String) ((TextView) header.findViewById(R.id.username)).getText());
        new Image((String) me.getProperty("avatarUrl"), "").displayIn(((ImageView) header.findViewById(R.id.avatar)));
        ((TextView) header.findViewById(R.id.email)).setText((String) me.getProperty("email"));
        return;
      }
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onPlacesChangedEvent(PlacesChangedEvent event) {
    QueryEnumerator enumerator = event.getEvent().getRows();
    for (int i = 0; i < enumerator.getCount(); i++) {
      QueryRow row = enumerator.getRow(i);
      Log.d(TAG, "Key is: " + row.getKey() + " and doc is: " + row.getDocument());
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onTripsChangedEvent(TripsChangedEvent event) {
    QueryEnumerator enumerator = event.getEvent().getRows();
    for (int i = 0; i < enumerator.getCount(); i++) {
      QueryRow row = enumerator.getRow(i);
      Log.d(TAG, "Key is: " + row.getKey() + " and doc is: " + row.getDocument() + " status is: " + row.getDocument().getProperty("status"));
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onFriendsChangedEvent(FriendsChangedEvent event) {
    QueryEnumerator enumerator = event.getEvent().getRows();
    for (int i = 0; i < enumerator.getCount(); i++) {
      QueryRow row = enumerator.getRow(i);
      Log.d(TAG, row.getKey() + " friendship between " + row.getDocument().getProperty("sender") + " and " + row.getDocument().getProperty("receiver"));
    }
  }
}
