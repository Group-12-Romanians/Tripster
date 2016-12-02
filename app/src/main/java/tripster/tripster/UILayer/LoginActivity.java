package tripster.tripster.UILayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.account.FacebookProvider;
import tripster.tripster.account.GoogleProvider;
import tripster.tripster.account.LoginProvider;

public class LoginActivity extends AppCompatActivity {

  private static final String TAG = LoginActivity.class.getName();

  private List<LoginProvider> loginProviders = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    loginProviders.add(new GoogleProvider(this));
    loginProviders.add(new FacebookProvider(this));

    for (LoginProvider lP : loginProviders) {
      lP.silentSignIn(); //TODO: This works only if Google (which logins synchronously) is the first in the list
    }

    setContentView(R.layout.activity_login);

    for (LoginProvider lP : loginProviders) {
      lP.setupLoginButton();
    }

    Log.d(TAG, "created LoginActivity and added possible Providers");
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
    i.putExtra("loginProvider", loginProviderClassName);
    i.putExtra("id", myId);
    if (name != null) {
      i.putExtra("name", name);
    }
    if (avatarUrl != null) {
      i.putExtra("avatarUrl", avatarUrl);
    }
    if (email != null) {
      i.putExtra("email", email);
    }
    startActivity(i);
    finish();
  }
}
