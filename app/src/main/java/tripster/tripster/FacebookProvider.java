package tripster.tripster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
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

public class FacebookProvider implements LoginProvider, AccountProvider {

    private AppCompatActivity parentActivity;
    private CallbackManager callbackManager;

    private static final int RC_FB_SIGN_IN = 64206;
    private static final String TAG = FacebookProvider.class.getName();

    // Initialize SDK and enter main activity if async login succeeds.
    public FacebookProvider(AppCompatActivity activity) {
        parentActivity = activity;

        FacebookSdk.sdkInitialize(parentActivity.getApplicationContext());

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken != null && parentActivity instanceof LoginActivity) {
                    ((LoginActivity) parentActivity).enterTripster(TAG);
                }
            }
        };
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_FB_SIGN_IN) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void logOut() {
        SharedPreferences sharedPref = parentActivity.getPreferences(Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        LoginManager.getInstance().logOut();
        Intent i = new Intent(parentActivity, LoginActivity.class);
        parentActivity.startActivity(i);
        parentActivity.finish();
    }

    @Override
    public void setUserAccountFields(final TextView name, final TextView email, final ImageView avatar) {
        AccessToken at = AccessToken.getCurrentAccessToken();
        Log.d(TAG, "token is" + (at == null ? "null" : at.getToken()));
        if (Utils.getInstance().internetConnection(parentActivity)) {
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject me, GraphResponse response) {
                    if (response.getError() != null) {
                        Log.d(TAG, response.getError().toString());
                    } else {
                        String username = me.optString("name");
                        Log.d(TAG, "name" + username);
                        name.setText(username);
                        String id = me.optString("id");
                        Log.d(TAG, "id" + id);
                        Utils.getInstance().setImageFromUrl("https://graph.facebook.com/" + id + "/picture?type=large", avatar);
                        String mail = "facebook@stupid.com";
                        email.setText(mail);

                        // save preferences
                        SharedPreferences sharedPref = parentActivity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("username", username);
                        editor.putString("email", mail);
                        editor.apply();
                    }
                }
            }).executeAsync();
        } else {
            SharedPreferences sharedPref = parentActivity.getPreferences(Context.MODE_PRIVATE);
            String username = sharedPref.getString("username", "John John"); //default value
            name.setText(username);

            String mail = sharedPref.getString("email", "john@john.com"); //default value
            email.setText(mail);
            //TODO: handle case when no internet for photo
        }
    }

    @Override
    public void setupLoginButton() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Facebook login successful");
                        ((LoginActivity) parentActivity).enterTripster(TAG);
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
}
