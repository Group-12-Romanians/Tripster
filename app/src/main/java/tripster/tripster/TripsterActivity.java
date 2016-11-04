package tripster.tripster;

import android.app.Activity;
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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.account.LogoutProvider;
import tripster.tripster.fragments.FriendsFragment;
import tripster.tripster.fragments.HomeFragment;
import tripster.tripster.fragments.PhotosOnMapFragment;
import tripster.tripster.fragments.PicturesFragment;
import tripster.tripster.fragments.TripsterFragment;
import tripster.tripster.services.LocationService;

public class TripsterActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private LogoutProvider accountProvider;
  private static final String TAG = TripsterActivity.class.getName();

  private Map<String, Fragment> fragments = null;
  private Activity activity = this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    recreateLoginSession();

    setContentView(R.layout.activity_tripster);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    View header = navigationView.getHeaderView(0);

    accountProvider.setUserAccountFields((TextView) header.findViewById(R.id.username),
        (TextView) header.findViewById(R.id.email), (ImageView) header.findViewById(R.id.avatar));

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    if (findViewById(R.id.main_content) != null) {
      if (fragments != null && savedInstanceState != null) { // This activity was already created once(but paused), so the fragment already exists
        Log.d(TAG, "fragments already exist");
        return;
      }
      fragments = new HashMap<>();

      fragments.put("initial", new TripsterFragment());
      Log.d(TAG, "Initialise TripsterFragment");

      fragments.put("friends", new FriendsFragment());
      Log.d(TAG, "Initialise FriendsFragment");

      fragments.put("pictures", new PicturesFragment());
      Log.d(TAG, "Initialise PicturesFragment");

      fragments.put("map", new PhotosOnMapFragment());
      Log.d(TAG, "Initialise PhotosOnMapFragment");

      fragments.put("home", new HomeFragment());
      Log.d(TAG, "Initialise HomeFragment");


      // Add the fragment to the 'main_container' FrameLayout
      getSupportFragmentManager().beginTransaction().add(R.id.main_content, fragments.get("initial")).commit();
    }

    final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.tracking_menu);
    final FloatingActionButton startButton = (FloatingActionButton) findViewById(R.id.tracking_start);
    final FloatingActionButton pauseButton = (FloatingActionButton) findViewById(R.id.tracking_pause);
    final FloatingActionButton endButton = (FloatingActionButton) findViewById(R.id.tracking_stop);

    if (isServiceRunning(LocationService.class)) {
      startButton.hideButtonInMenu(true);
    } else {
      pauseButton.hideButtonInMenu(true);
      endButton.hideButtonInMenu(true);
    }

    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to start recording");

        if (!isServiceRunning(LocationService.class)) {
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
//        Log.d(TAG, "wanna switch to pause recording");
//        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
//        serviceIntent.putExtra("flag", "pause");
//        TripsterActivity.this.startService(serviceIntent);
//
//        pauseButton.hideButtonInMenu(true);
//        endButton.showButtonInMenu(true);
//        startButton.setLabelText("Resume Trip");
//        startButton.showButtonInMenu(true);
      }
    });

    endButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent serviceIntent;
        if (!isServiceRunning(LocationService.class)) {
//          Log.d(TAG, "wanna switch to start recording");
//          serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
//          serviceIntent.putExtra("flag", "start");
//          TripsterActivity.this.startService(serviceIntent);
        }
        if (isServiceRunning(LocationService.class)) {
          Log.d(TAG, "wanna switch to stop recording");
          serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
          serviceIntent.putExtra("flag", "stop");
          TripsterActivity.this.startService(serviceIntent);

          pauseButton.hideButtonInMenu(true);
          endButton.hideButtonInMenu(true);
          startButton.setLabelText("Start Trip");
          startButton.showButtonInMenu(true);
        }
      }
    });
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

    Fragment frag = fragments.get("initial");

    if (id == R.id.nav_camera) {
      Log.d(TAG, "I want to switch to initial fragment");
    } else if (id == R.id.nav_gallery) {
      frag = fragments.get("pictures");
      Log.d(TAG, "I want to switch to pictures fragment");
    } else if (id == R.id.nav_slideshow) {
      frag = fragments.get("friends");
      Log.d(TAG, "I want to switch to friends fragment");
    } else if (id == R.id.nav_manage) {
      frag = fragments.get("map");
      Log.d(TAG, "I want to switch to map fragment");
    } else if (id == R.id.home) {
      frag = fragments.get("home");
      Log.d(TAG, "I want to switch to home fragment");
    } else if (id == R.id.nav_share) {

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

  private boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        Log.i("isMyServiceRunning?", true + "");
        return true;
      }
    }
    Log.i("isMyServiceRunning?", false + "");
    return false;
  }
}
