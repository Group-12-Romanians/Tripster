package tripster.tripster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

    public void setImageFromUrl(String s, ImageView avatar) {
        new GetBitmapFromUrlTask(avatar).execute(s);
    }

    private static Utils utils;

    public static Utils getInstance() {
        if (utils == null) {
            utils = new Utils();
        }
        return utils;
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
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromURL(params[0]);
        }
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }
}
