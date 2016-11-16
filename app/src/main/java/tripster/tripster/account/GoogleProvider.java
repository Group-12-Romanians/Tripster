package tripster.tripster.account;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import tripster.tripster.LoginActivity;
import tripster.tripster.R;

public class GoogleProvider extends AccountProvider {

  private static final int RC_GOOGLE_SIGN_IN = 9001;
  private static final String TAG = GoogleProvider.class.getName();

  private GoogleApiClient googleApiClient;

  public GoogleProvider(AppCompatActivity activity) {
    parentActivity = activity;
    GoogleSignInOptions gso = new GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
    googleApiClient = new GoogleApiClient.Builder(parentActivity)
        .enableAutoManage(parentActivity,
                          new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult connectionResult) {
            Toast.makeText(parentActivity, "Login canceled", Toast.LENGTH_LONG).show();
          }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();
  }

  @Override
  public boolean isLoggedIn() {
    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
    if (opr.isDone()) {
      Log.d(TAG, "Google cached sign-in");
      GoogleSignInResult result = opr.get();
      handleSignInResult(result);
      return true;
    }
    return false;
  }

  @Override
  public void setupLoginButton() {
    parentActivity
        .findViewById(R.id.sign_in_button)
        .setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent signInIntent =
            Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        parentActivity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
      }
    });
  }

  @Override
  public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_GOOGLE_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    Log.d(TAG, "Google handleSignInResult:" + result.isSuccess());
    if (result.isSuccess() && parentActivity instanceof LoginActivity) {
      Log.d(TAG, "Google login successful");
      String email = result.getSignInAccount().getEmail();
      String name = result.getSignInAccount().getDisplayName();
      saveUser(email, name, TAG);
    } else {
      Toast.makeText(parentActivity, "Login canceled", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void setUserAccountFields(final TextView name,
                                   final TextView email,
                                   final ImageView avatar) {
    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
    if (opr.isDone()) {
      GoogleSignInResult result = opr.get();
      GoogleSignInAccount account = result.getSignInAccount();

      String username = account.getDisplayName();
      name.setText(username);
      Log.d(TAG, "username set:" + username);

      email.setText(account.getEmail());

      Uri avatarUrl = account.getPhotoUrl();
      if (internetConnection(parentActivity)) {
        if (avatarUrl != null) {
          Log.d(TAG, "avatar:" + avatarUrl.toString());
          setAvatarFromUrl(avatarUrl.toString(), avatar);
        }
      } else {
        setAvatarFromCache(avatar);
      }
    }
  }

  @Override
  public void logOut() {
    removeAvatar();
    clearCacheData();
    Auth.GoogleSignInApi
        .signOut(googleApiClient)
        .setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        switchToLogin();
      }
    });
  }
}
