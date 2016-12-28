package tripster.tripster.UILayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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

import com.couchbase.lite.Document;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;
import net.grandcentrix.tray.core.TrayItem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.Image;
import tripster.tripster.R;
import tripster.tripster.UILayer.newsfeed.NewsfeedFragment;
import tripster.tripster.UILayer.notifications.NotificationsFragment;
import tripster.tripster.UILayer.users.AllUsersFragment;
import tripster.tripster.UILayer.users.MyProfileFragment;
import tripster.tripster.account.LogoutProvider;
import tripster.tripster.dataLayer.TripsterDb;
import tripster.tripster.services.LocationService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.CURR_TRIP_ID;
import static tripster.tripster.Constants.CURR_TRIP_ST;
import static tripster.tripster.Constants.LOGIN_PROVIDER;
import static tripster.tripster.Constants.MY_ID;
import static tripster.tripster.Constants.PAUSE_SERVICE;
import static tripster.tripster.Constants.RESUME_SERVICE;
import static tripster.tripster.Constants.START_SERVICE;
import static tripster.tripster.Constants.STOP_SERVICE;
import static tripster.tripster.Constants.TRIP_PAUSED;
import static tripster.tripster.Constants.TRIP_RUNNING;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_EMAIL_K;
import static tripster.tripster.Constants.USER_ID;
import static tripster.tripster.Constants.USER_NAME_K;

public class TripsterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = TripsterActivity.class.getName();
  private static final int MY_PERMISSIONS_REQUEST = 47;

  public static TripsterDb tDb;
  public static String currentUserId;

  private AppPreferences pref;
  private LogoutProvider accountProvider;

  private DrawerLayout drawer;
  private View header;

  // User info
  private ImageView avatar;
  private TextView name;
  private TextView email;

  // Current trip status
  private FloatingActionMenu menu;
  private FloatingActionButton start;
  private FloatingActionButton pause;
  private FloatingActionButton resume;
  private FloatingActionButton stop;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    tDb = TripsterDb.getInstance(getApplicationContext());
    tDb.initAllViews();
    tDb.startSync();

    pref = new AppPreferences(getApplicationContext());

    setContentView(R.layout.activity_tripster);
    askForPermissions();
    initializeDrawer();
    Fragment frag = new NewsfeedFragment();
    getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String perms[], @NonNull int[] grantResults) {
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
    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    header = navigationView.getHeaderView(0);
    navigationView.setNavigationItemSelectedListener(this);

    name = (TextView) header.findViewById(R.id.username);
    email = (TextView) header.findViewById(R.id.email);
    avatar = (ImageView) header.findViewById(R.id.avatar);
  }

  @Override
  protected void onStart() {
    super.onStart();

    String userId = getIntent().getStringExtra(USER_ID);
    String name = getIntent().getStringExtra(USER_NAME_K);
    String email = getIntent().getStringExtra(USER_EMAIL_K);
    String avatarUrl = getIntent().getStringExtra(USER_AVATAR_K);

    currentUserId = userId;
    pref.put(MY_ID, userId);
    Log.d(TAG, "The current user ID is: " + userId);

    recreateLoginSession();

    initializeTrackingButtons();
    initializeFab();

    //Update or Create the current User
    Map<String, Object> props = new HashMap<>();
    props.put(USER_NAME_K, name);
    props.put(USER_EMAIL_K, email);
    props.put(USER_AVATAR_K, avatarUrl);
    tDb.upsertNewDocById(userId, props);
  }

  private void initializeTrackingButtons() {
    menu = (FloatingActionMenu) findViewById(R.id.tracking_menu);

    start = (FloatingActionButton) findViewById(R.id.tracking_start);
    pause = (FloatingActionButton) findViewById(R.id.tracking_pause);
    resume = (FloatingActionButton) findViewById(R.id.tracking_resume);
    stop = (FloatingActionButton)findViewById(R.id.tracking_stop);
  }

  private void recreateLoginSession() {
    String loginProviderClassName = getIntent().getStringExtra(LOGIN_PROVIDER);
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

  private void initializeFab() {
    updateFabState();

    start.setOnClickListener(getFABListener(START_SERVICE));
    pause.setOnClickListener(getFABListener(PAUSE_SERVICE));
    resume.setOnClickListener(getFABListener(RESUME_SERVICE));
    stop.setOnClickListener(getFABListener(STOP_SERVICE));
  }

  private View.OnClickListener getFABListener(final String flag) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "wanna switch to " + flag + " recording");
        Intent serviceIntent = new Intent(TripsterActivity.this, LocationService.class);
        serviceIntent.putExtra("flag", flag);
        TripsterActivity.this.startService(serviceIntent);
      }
    };
  }

  private void updateFabState() {
    String currentTripId = pref.getString(CURR_TRIP_ID, "");
    String currentTripState = pref.getString(CURR_TRIP_ST, "");
    assertNotNull(currentTripId);
    assertNotNull(currentTripState);
    Log.d(TAG, "Called this with: " + currentTripId + " and " + currentTripState);

    if (!menu.isOpened()) {
      menu.open(false);
    }
    if (currentTripId.isEmpty()) {
      //start button only
      start.showButtonInMenu(true);
      pause.hideButtonInMenu(true);
      resume.hideButtonInMenu(true);
      stop.hideButtonInMenu(true);
    } else if (currentTripState.equals(TRIP_PAUSED)) {
      //resume and stop buttons
      start.hideButtonInMenu(true);
      pause.hideButtonInMenu(true);
      resume.showButtonInMenu(true);
      stop.showButtonInMenu(true);
    } else if (currentTripState.equals(TRIP_RUNNING)) {
      //pause and stop buttons
      start.hideButtonInMenu(true);
      resume.hideButtonInMenu(true);
      pause.showButtonInMenu(true);
      stop.showButtonInMenu(true);
    }
  }

  @Override
  public void onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    Fragment frag = null;
    if (id == R.id.nav_camera) {
      frag = new MyProfileFragment();
      Bundle args = new Bundle();
      args.putString(USER_ID, currentUserId);
      frag.setArguments(args);
      Log.d(TAG, "I want to switch to MyProfile fragment");
    } else if (id == R.id.nav_slideshow) {
      frag = new AllUsersFragment();
      Log.d(TAG, "I want to switch to AllUsers fragment");
    } else if (id == R.id.news_feed) {
      frag = new NewsfeedFragment();
      Log.d(TAG, "I want to switch to NewsFeed fragment");
    } else if (id == R.id.nav_send) {
      frag = new NotificationsFragment();
      Log.d(TAG, "I want to switch to Notifications fragment");
    } else if (id == R.id.nav_logout) {
      Log.d(TAG, "I want to switch to logout");
      accountProvider.logOut();
      return true;
    }

    if (frag != null) {
      getSupportFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
    }

    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  //----------------------------------------- LISTENERS ------------------------------------------//
  @Override
  protected void onResume() {
    super.onResume();
    initializeHeader();
    tDb.getDocumentById(currentUserId).addChangeListener(currentUserChangeListener);
    pref.registerOnTrayPreferenceChangeListener(currentTripChangeListener);
  }

  OnTrayPreferenceChangeListener currentTripChangeListener = new OnTrayPreferenceChangeListener() {
    @Override
    public void onTrayPreferenceChanged(Collection<TrayItem> items) {
      updateFabState();
    }
  };

  private Document.ChangeListener currentUserChangeListener = new Document.ChangeListener() {
    @Override
    public void changed(Document.ChangeEvent event) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          initializeHeader();
        }
      });
    }
  };

  private void initializeHeader() {
    Document me = tDb.getDocumentById(currentUserId);
    Log.d(TAG, "Current user changed, id is:" + me.getId());

    name.setText((String) me.getProperty(USER_NAME_K));
    Log.d(TAG, "Current user's new name is:" + (name.getText()));

    new Image((String) me.getProperty(USER_AVATAR_K)).displayIn(avatar);
    Log.d(TAG, "Current user's new avatarUrl should be:" + me.getProperty(USER_AVATAR_K));

    email.setText((String) me.getProperty(USER_EMAIL_K));
    Log.d(TAG, "Current user's new email is:" + email.getText());
  }

  @Override
  protected void onPause() {
    try {
      tDb.getDocumentById(currentUserId).removeChangeListener(currentUserChangeListener);
      pref.unregisterOnTrayPreferenceChangeListener(currentTripChangeListener);
    } catch (NullPointerException e) {
      Log.e(TAG, "Something failed");
    }
    super.onPause();
  }
}
