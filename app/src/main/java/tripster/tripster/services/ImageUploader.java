package tripster.tripster.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tripster.tripster.dataLayer.TripsterDb;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.MAX_SIZE;
import static tripster.tripster.Constants.PHOTO_PLACE_K;
import static tripster.tripster.Constants.PHOTO_TIME_K;
import static tripster.tripster.Constants.PHOTO_TRIP_K;
import static tripster.tripster.Constants.SERVER_URL;

public class ImageUploader extends AsyncTask<Void, Void, Void> {
  private static final String CRLF = "\r\n";
  private static final String TWO_HYPHENS = "--";
  private static final String BOUNDARY = "*****";

  private Context context;
  private String status;

  public ImageUploader(Context context) {
    this.context = context;
  }

  @Override
  protected Void doInBackground(Void... params) {
    Map<String, ?> photos = context.getSharedPreferences("photos", Context.MODE_PRIVATE).getAll();
    for (String photo : photos.keySet()) {
      uploadPhoto(photo, context.getSharedPreferences("photos", Context.MODE_PRIVATE).getString(photo, "error"));
    }
    return null;
  }

  private void uploadPhoto(String photoPath, String photoDocStr) {
    String photoId = UUID.randomUUID().toString();
    HttpURLConnection httpUrlConnection;
    URL url = null;
    try {
      url = new URL(SERVER_URL + "/photos/upload?photo_id=" + photoId);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    try {
      assertNotNull(url);
      httpUrlConnection = (HttpURLConnection) url.openConnection();
      httpUrlConnection.setUseCaches(false);
      httpUrlConnection.setDoOutput(true);
      httpUrlConnection.setRequestMethod("POST");
      httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
      httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
      httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

      DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
      request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
      request.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"" + photoId + ".jpg" + "\"" + CRLF);
      request.writeBytes(CRLF);

      Bitmap bm = getCorrectedBitmap(photoPath);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
      byte[] bytes = baos.toByteArray();
      request.write(bytes);

      request.writeBytes(CRLF);
      request.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);

      request.flush();
      request.close();

      InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());

      BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

      String line;
      StringBuilder stringBuilder = new StringBuilder();

      while ((line = responseStreamReader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }

      responseStreamReader.close();

      responseStream.close();
      httpUrlConnection.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    context.getSharedPreferences("photos", Context.MODE_PRIVATE).edit().remove(photoPath).commit();
    TripsterDb tDb = TripsterDb.getInstance(context.getApplicationContext());
    try {
      JSONObject photoDoc = new JSONObject(photoDocStr);
      Map<String, Object> props = new HashMap<>();
      props.put(PHOTO_PLACE_K, photoDoc.get(PHOTO_PLACE_K));
      props.put(PHOTO_TRIP_K, photoDoc.get(PHOTO_TRIP_K));
      props.put(PHOTO_TIME_K, photoDoc.get(PHOTO_TIME_K));
      tDb.upsertNewDocById(photoId, props);
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }

    status = "running";
    tDb.pushChanges(new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        status = response;
      }
    });
    while(status.equals("running"));
  }


  private Bitmap getCorrectedBitmap(String photoPath) throws IOException{
    File imageFile = new File(photoPath);
    FileInputStream fis = new FileInputStream(imageFile);

    Bitmap bm = BitmapFactory.decodeStream(fis);
    ExifInterface exif = new ExifInterface(photoPath);
    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

    int rotate = 0;
    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_270:
        rotate = 270;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        rotate = 180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_90:
        rotate = 90;
        break;
    }
    Matrix matrix = new Matrix();
    matrix.postRotate(rotate);
    Bitmap rBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    return bitmapResize(rBm, MAX_SIZE);
  }

  private Bitmap bitmapResize(Bitmap bitmap, int maxSize) {
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();
      float ratioBitmap = (float) width / (float) height;

      int finalWidth = maxSize;
      int finalHeight = maxSize;
      if (ratioBitmap > 1) {
        finalHeight = (int) ((float) finalWidth / ratioBitmap);
      } else {
        finalWidth = (int) ((float) finalHeight * ratioBitmap);
      }
      bitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
      return bitmap;
  }
}