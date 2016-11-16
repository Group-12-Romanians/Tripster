package tripster.tripster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.account.LogoutProvider;
import tripster.tripster.friends.FriendsFragment;
import tripster.tripster.fragments.NewsFeedFragment;
import tripster.tripster.services.LocationService;
import tripster.tripster.trips.MyTripsFragment;

public class TripsterActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  public static final String SERVER_URL = "http://146.169.46.220:8081";
  public static final String LOCATIONS_FILE_PATH = "locations.txt";
  public static final String SHARED_PREF_PHOTOS = "TripsterPhotosIds";
  public static final String SHARED_PREF_ID = "TripsterID";
  private static final String TAG = TripsterActivity.class.getName();

  public static String USER_ID = "";
  public static RequestQueue reqQ;

  private LogoutProvider accountProvider;

  private Map<String, Fragment> fragments = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    recreateLoginSession();

    reqQ = Volley.newRequestQueue(this);

    setContentView(R.layout.activity_tripster);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    View header = navigationView.getHeaderView(0);

    accountProvider.setUserAccountFields((TextView) header.findViewById(R.id.username),
        (TextView) header.findViewById(R.id.email), (ImageView) header.findViewById(R.id.avatar));

    USER_ID = accountProvider.getUserId();

    Log.d(TAG, "User ID is: " + USER_ID);

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    if (findViewById(R.id.main_content) != null) {
      if (fragments != null && savedInstanceState != null) { // This activity was already created once(but paused), so the fragment already exists
        Log.d(TAG, "fragments already exist");
        return;
      }
      fragments = new HashMap<>();

      fragments.put("friends", new FriendsFragment());
      Log.d(TAG, "Initialise FriendsFragment");

      fragments.put("myTrips", new MyTripsFragment());
      Log.d(TAG, "Initialise MyTripsFragment");

      fragments.put("newsFeed", new NewsFeedFragment());
      Log.d(TAG, "Initialise news feed");

      // Add the fragment to the 'main_container' FrameLayout
      getSupportFragmentManager().beginTransaction().add(R.id.main_content, fragments.get("myTrips")).commit();
    }

    initializeFab();
  }

  private void initializeFab() {
    final FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.tracking_start);
    final FloatingActionButton pauseButton = (FloatingActionButton) findViewById(R.id.tracking_pause);
    final FloatingActionButton resumeButton = (FloatingActionButton) findViewById(R.id.tracking_resume);
    final FloatingActionButton endButton = (FloatingActionButton) findViewById(R.id.tracking_stop);

    switch(getServiceStatus()) {
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
        if (!isServiceRunning()) {
          Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
          serviceIntent.putExtra("flag", "start");
          TripsterActivity.this.startService(serviceIntent);

          startButton.hideButtonInMenu(true);
          pauseButton.showButtonInMenu(true);
          endButton.showButtonInMenu(true);
        }
      }
    });

    pauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to pause recording");
        if (isServiceRunning()) {
          Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
          serviceIntent.putExtra("flag", "pause");
          TripsterActivity.this.startService(serviceIntent);

          pauseButton.hideButtonInMenu(true);
          resumeButton.showButtonInMenu(true);
        }
      }
    });

    resumeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to pause recording");
        if (!isServiceRunning()) {
          Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
          serviceIntent.putExtra("flag", "resume");
          TripsterActivity.this.startService(serviceIntent);

          resumeButton.hideButtonInMenu(true);
          pauseButton.showButtonInMenu(true);
        }
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
    if (isServiceRunning()) {
      return "running";
    }
    if (locationFileExists()) {
      return "paused";
    }
    return "stopped";
  }

  private boolean locationFileExists() {
    File file = new File(getFilesDir(), LOCATIONS_FILE_PATH);
    return file.exists();
  }

  private boolean isServiceRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (LocationService.class.getName().equals(service.service.getClassName())) {
        Log.i("isMyServiceRunning?", true + "");
        return true;
      }
    }
    Log.i("isMyServiceRunning?", false + "");
    return false;
  }

  private void recreateLoginSession() {
    String loginProviderClassName = getIntent().getStringExtra("loginProvider");
    Log.d(TAG, loginProviderClassName);
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    Fragment frag = fragments.get("myTrips");
    if (id == R.id.nav_camera) {
      Log.d(TAG, "I want to switch to myTrips fragment");
    } else if (id == R.id.nav_slideshow) {
      frag = fragments.get("friends");
      Log.d(TAG, "I want to switch to friends fragment");
    } else if (id == R.id.news_feed) {
      frag = fragments.get("newsFeed");
      Log.d(TAG, "I want to switch to newsFeed fragment");

    } else if (id == R.id.nav_send) {

    } else if (id == R.id.nav_logout) {
      accountProvider.logOut();
      return true;
    }

    getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
