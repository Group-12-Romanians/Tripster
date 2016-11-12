package tripster.tripster.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import tripster.tripster.LoginActivity;

abstract class AccountProvider implements LoginProvider, LogoutProvider {
  private static final String TAG = AccountProvider.class.getName();
  public static final String SHARED_PREF_NAME = "Tripster";
  AppCompatActivity parentActivity;

  boolean saveUser(String id, String name, String tag) {
    RequestQueue requestQueue = Volley.newRequestQueue(parentActivity);
    if (internetConnection(parentActivity)) {
      StringRequest userRequest = createUserRequest(id, name, tag);
      requestQueue.add(userRequest);
      Log.d(TAG, "Here is my user id: " + id);
      return true;
    }
    return false;
  }

  private StringRequest createUserRequest(final String id, final String name, final String tag) {
    String userURL = "http://146.169.46.220:8081/new_user";
    return new StringRequest(Request.Method.POST,
        userURL,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "Response from user db is: " + response);
            try {
              JSONObject userObject = new JSONObject(response);
              SharedPreferences sharedPref = parentActivity
                  .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
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
        },
        new Response.ErrorListener() {
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

        return params;
      }
    };
  }

  @Override
  public String getUserId() {
    SharedPreferences sharedPref = parentActivity
        .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    return sharedPref.getString("id", "");
  }

  void clearCacheData() {
    SharedPreferences sharedPref = parentActivity
        .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    sharedPref.edit().clear().apply();
    removeAvatar();
  }

  void switchToLogin() {
    Intent i = new Intent(parentActivity, LoginActivity.class);
    parentActivity.startActivity(i);
    parentActivity.finish();
  }

  boolean internetConnection(AppCompatActivity activity) {
    ConnectivityManager connectivityManager = (ConnectivityManager) activity
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  void setAvatarFromUrl(String s, ImageView avatar) {
    new GetBitmapFromUrlTask(avatar).execute(s);
  }

  void setAvatarFromCache(ImageView avatar) {
    String pathName = parentActivity.getFilesDir() + "/avatarPic.jpg";
    Log.d("filePathOUT", pathName);
    avatar.setImageBitmap(BitmapFactory.decodeFile(pathName));
  }

  void removeAvatar() {
    String pathName = parentActivity.getFilesDir() + "/avatarPic.jpg";
    Log.d("filePathREMOVE", pathName);
    File avatarFile = new File(pathName);
    avatarFile.delete();
  }

  private class GetBitmapFromUrlTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewRef;

    private GetBitmapFromUrlTask(ImageView imageView) {
      imageViewRef = new WeakReference<>(imageView);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap != null) {
        final ImageView imageView = imageViewRef.get();
        if (imageView != null) {
          imageView.setImageBitmap(bitmap);
        }

        try {
          File file = new File(parentActivity.getFilesDir(), "avatarPic.jpg");
          Log.d("filePathIN", file.getAbsolutePath());
          FileOutputStream out = new FileOutputStream(file);
          bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      try {
        URL url = new URL(params[0]);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        return BitmapFactory.decodeStream(input);
      } catch (IOException e) {
        return null;
      }
    }
  }
}
