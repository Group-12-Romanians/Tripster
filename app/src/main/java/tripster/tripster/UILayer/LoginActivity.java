package tripster.tripster.UILayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jrummyapps.android.widget.AnimatedSvgView;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.account.FacebookProvider;
import tripster.tripster.account.GoogleProvider;
import tripster.tripster.account.LoginProvider;

import static tripster.tripster.Constants.LOGIN_PROVIDER;
import static tripster.tripster.Constants.USER_AVATAR_K;
import static tripster.tripster.Constants.USER_EMAIL_K;
import static tripster.tripster.Constants.USER_ID;
import static tripster.tripster.Constants.USER_NAME_K;

public class LoginActivity extends AppCompatActivity {

  private static final String TAG = LoginActivity.class.getName();

  private List<LoginProvider> loginProviders = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_login);

    final AnimatedSvgView logo = (AnimatedSvgView) findViewById(R.id.animated_svg_view);
    logo.start();
    logo.setOnStateChangeListener(new AnimatedSvgView.OnStateChangeListener() {
      @Override
      public void onStateChange(int state) {
        if (state == AnimatedSvgView.STATE_FINISHED) {
          startDelayed(logo);
        }
      }
    });

    loginProviders.add(new GoogleProvider(this));
    loginProviders.add(new FacebookProvider(this));

    for (LoginProvider lP : loginProviders) {
      lP.silentSignIn(); //TODO: This works only if Google (which logins synchronously) is the first in the list
    }

    for (LoginProvider lP : loginProviders) {
      lP.setupLoginButton();
    }

    Log.d(TAG, "created LoginActivity and added possible Providers");
  }

  private void startDelayed(final AnimatedSvgView logo) {
    logo.postDelayed(new Runnable() {
      @Override
      public void run() {
        logo.start();
      }
    }, 2000);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    for (LoginProvider lP : loginProviders) {
      lP.handleActivityResult(requestCode, resultCode, data);
    }
  }

  public void handleLogin(String loginProviderClassName, String myId, String name, String avatarUrl, String email) {
    Intent i = new Intent(LoginActivity.this, TripsterActivity.class);
    i.putExtra(LOGIN_PROVIDER, loginProviderClassName);
    i.putExtra(USER_ID, myId);
    if (name != null) {
      i.putExtra(USER_NAME_K, name);
    }
    if (avatarUrl != null) {
      i.putExtra(USER_AVATAR_K, avatarUrl);
    }
    if (email != null) {
      i.putExtra(USER_EMAIL_K, email);
    }
    startActivity(i);
    finish();
  }
}
