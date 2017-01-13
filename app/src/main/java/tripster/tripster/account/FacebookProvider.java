package tripster.tripster.account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

import tripster.tripster.R;

public class FacebookProvider extends AccountProvider {

  private static final int RC_FB_SIGN_IN = 64206;
  private static final String TAG = FacebookProvider.class.getName();

  private CallbackManager callbackManager;

  public FacebookProvider(AppCompatActivity activity) {
    parentActivity = activity;
    FacebookSdk.sdkInitialize(parentActivity.getApplicationContext());
  }

  @Override
  public void silentSignIn() {
//    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() { //TODO: How the fuck does this remain in memory after we exit the scope??? (it does, but why)
//      @Override
//      protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
//        if (newAccessToken != null && parentActivity instanceof LoginActivity) {
//          signInWith(newAccessToken);
//        }
//      }
//    };
  }

  @Override
  public void setupLoginButton() {
    callbackManager = CallbackManager.Factory.create();
    LoginManager.getInstance().registerCallback(callbackManager,
        new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
            signInWith(loginResult.getAccessToken());
          }

          @Override
          public void onCancel() {
            Toast.makeText(parentActivity, "Login canceled", Toast.LENGTH_LONG).show();
          }

          @Override
          public void onError(FacebookException exception) {
            Toast.makeText(parentActivity, exception.getMessage(), Toast.LENGTH_LONG).show();
          }
        });

    Button fbLoginButton = (Button) parentActivity.findViewById(R.id.btn_fb_login);

    fbLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        LoginManager.getInstance().logInWithReadPermissions(parentActivity, Arrays.asList("public_profile", "email"));
      }
    });
  }

  @Override
  public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_FB_SIGN_IN) {
      callbackManager.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void logOut() {
    LoginManager.getInstance().logOut();
    super.logOut();
  }

  private void signInWith(final AccessToken token) {
    GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
      @Override
      public void onCompleted(JSONObject object, GraphResponse response) {
        if (response.getError() != null) {
          Log.w(TAG, "Could not get user data because: " + response.getError().toString());
          login(TAG, token.getUserId());
        } else {
          String id = object.optString("id");
          String avatarUrl = "https://graph.facebook.com/" + id + "/picture?type=large";
          login(TAG, id, object.optString("name"), avatarUrl, object.optString("email"));
        }
      }
    }).executeAsync();
  }
}
