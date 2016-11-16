package tripster.tripster.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

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

import static tripster.tripster.TripsterActivity.SERVER_URL;

public class ImageUploader extends AsyncTask<String, Void, String> {
  private static final String CRLF = "\r\n";
  private static final String TWO_HYPHENS = "--";
  private static final String BOUNDARY = "*****";

  @Override
  protected void onPostExecute(String s) {
    Log.d("ONPOSTEXEC RESPONSE IS:", s);
  }

  @Override
  protected String doInBackground(String... params ) {
    String response = "NO RESPONSE";

    HttpURLConnection httpUrlConnection = null;
    String photoId = params[0];
    String photoPath = params[1];
    URL url = null;
    try {
      url = new URL(SERVER_URL + "/photos/upload?photo_id=" + photoId);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    try {
      httpUrlConnection = (HttpURLConnection) url.openConnection();
      httpUrlConnection.setUseCaches(false);
      httpUrlConnection.setDoOutput(true);

      httpUrlConnection.setRequestMethod("POST");
      httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
      httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
      httpUrlConnection.setRequestProperty(
          "Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

      DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
      request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
      request.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"" +
          photoId + ".jpg" + "\"" + CRLF);
      request.writeBytes(CRLF);

      File imageFile = new File(photoPath);
      FileInputStream fis = new FileInputStream(imageFile);

      Bitmap bm = BitmapFactory.decodeStream(fis);
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

      while ((line = responseStreamReader.readLine()) != null)

      {
        stringBuilder.append(line).append("\n");
      }

      responseStreamReader.close();

      response = stringBuilder.toString();
      Log.d("RESPONSE IS:", response);

      responseStream.close();

      httpUrlConnection.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return response;
  }
}