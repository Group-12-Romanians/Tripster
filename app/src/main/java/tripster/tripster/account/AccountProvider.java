package tripster.tripster.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.LoginActivity;
import tripster.tripster.trips.pictures.Photo;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static tripster.tripster.TripsterActivity.SHARED_PREF_ID;

abstract class AccountProvider implements LoginProvider, LogoutProvider {

  private static final String TAG = AccountProvider.class.getName();

  AppCompatActivity parentActivity;

  boolean saveUser(String id, String name, String photoUrl, String tag) {
    RequestQueue requestQueue = Volley.newRequestQueue(parentActivity);
    if (internetConnection(parentActivity)) {
      StringRequest userRequest = createUserRequest(id, name, photoUrl, tag);
      requestQueue.add(userRequest);
      Log.d(TAG, "Here is my user id: " + id);
      return true;
    }
    Toast.makeText(parentActivity, "No network connection.", Toast.LENGTH_LONG).show();
    return false;
  }

  private StringRequest createUserRequest(final String id, final String name, final String photoUrl, final String tag) {
    String userURL = "http://146.169.46.220:8081/new_user";
    return new StringRequest(Request.Method.POST, userURL, new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        Log.d(TAG, "Response from user db is: " + response);
        try {
          JSONObject userObject = new JSONObject(response);
          SharedPreferences sharedPref = parentActivity
              .getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
          sharedPref
              .edit()
              .putString("id", userObject.getString("_id").replace("\"", ""))
              .apply();

          if (parentActivity instanceof LoginActivity) {
              ((LoginActivity) parentActivity).handleLogin(tag);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Unable to save user.");
      }
    }) {
      @Override
      public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Log.d(TAG, "Parameters were assigned");
        Log.d(TAG, "Parameter is: " + id);
        params.put("id", id);
        params.put("name", name);
        params.put("avatar", photoUrl);

        return params;
      }
    };
  }

  @Override
  public String getUserId() {
    SharedPreferences sharedPref = parentActivity.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
    return sharedPref.getString("id", "");
  }

  void clearCacheData() {
    SharedPreferences sharedPref = parentActivity.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
    sharedPref.edit().clear().apply();
    removeAvatar();
  }

  void switchToLogin() {
    Intent i = new Intent(parentActivity, LoginActivity.class);
    parentActivity.startActivity(i);
    parentActivity.finish();
  }

  boolean internetConnection(AppCompatActivity activity) {
    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  void setAvatarFromUrl(String s, ImageView avatar) {
    new Photo(s, "").displayIn(avatar);
  }

  void setAvatarFromCache(ImageView avatar) {
    String pathName = parentActivity.getFilesDir() + "/avatarPic.jpg";
    Log.d("filePathOUT", pathName);
    new Photo(pathName, "").displayIn(avatar);
  }

  void removeAvatar() {
    String pathName = parentActivity.getFilesDir() + "/avatarPic.jpg";
    Log.d("filePathREMOVE", pathName);
    File avatarFile = new File(pathName);
    avatarFile.delete();
  }
}
