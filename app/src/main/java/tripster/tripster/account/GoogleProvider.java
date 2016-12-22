package tripster.tripster.account;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import tripster.tripster.UILayer.LoginActivity;
import tripster.tripster.R;

public class GoogleProvider extends AccountProvider {

  private static final int RC_GOOGLE_SIGN_IN = 9001;
  private static final String TAG = GoogleProvider.class.getName();

  private GoogleApiClient googleApiClient;

  public GoogleProvider(AppCompatActivity activity) {
    parentActivity = activity;
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
    googleApiClient = new GoogleApiClient.Builder(parentActivity)
        .enableAutoManage(parentActivity, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(parentActivity, "Login canceled", Toast.LENGTH_LONG).show();
          }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
  }

  @Override
  public void silentSignIn() {
    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
    if (opr.isDone()) {
      Log.d(TAG, "Google slient sign-in");
      GoogleSignInResult result = opr.get();
      handleSignIn(result);
    }
  }

  @Override
  public void setupLoginButton() {
    parentActivity.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        parentActivity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
      }
    });
  }

  @Override
  public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_GOOGLE_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignIn(result);
    }
  }

  @Override
  public void logOut() {
    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(Status status) {
        GoogleProvider.super.logOut();
      }
    });
  }

  private void handleSignIn(GoogleSignInResult result) {
    Log.d(TAG, "Google signed in: " + result.isSuccess());
    if (result.isSuccess() && parentActivity instanceof LoginActivity) {
      Log.d(TAG, "Google login successful");
      GoogleSignInAccount a = result.getSignInAccount();
      login(TAG, a.getId(), a.getDisplayName(), a.getPhotoUrl().toString(), a.getEmail());
    } else {
      Toast.makeText(parentActivity, "Login canceled", Toast.LENGTH_LONG).show();
    }
  }
}
