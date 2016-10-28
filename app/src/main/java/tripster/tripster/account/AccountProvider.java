package tripster.tripster.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import tripster.tripster.LoginActivity;

abstract class AccountProvider implements LoginProvider, LogoutProvider {
    AppCompatActivity parentActivity;

    void switchToLogin() {
        Intent i = new Intent(parentActivity, LoginActivity.class);
        parentActivity.startActivity(i);
        parentActivity.finish();
    }

    boolean internetConnection(AppCompatActivity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
