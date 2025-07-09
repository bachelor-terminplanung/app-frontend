package at.terminplaner;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class VisionApiHelper {

    public static void recognizeHandwriting(Bitmap bitmap, String apiKey, Consumer<String> callback) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 1024 * bitmap.getHeight() / bitmap.getWidth(), true);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream); // format to jpeg and store in byteArrayOutputStream
                String base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP); // jpeg to base64
                Log.d("64", "base 64 text: " + base64Image);

                JSONObject requestObj = new JSONObject();
                Log.d("JSON", "requestObj: " + requestObj);
                JSONObject image = new JSONObject();
                Log.d("JSON", "image: " + image);

                image.put("content", base64Image);
                Log.d("JSON", "image: " + image);

                JSONObject feature = new JSONObject();
                feature.put("type", "DOCUMENT_TEXT_DETECTION");
                Log.d("JSON", "feature: " + feature);

                JSONObject request = new JSONObject();
                request.put("image", image);
                request.put("features", new JSONArray().put(feature));
                Log.d("JSON", "request: " + request);

                JSONArray requests = new JSONArray().put(request);
                requestObj.put("requests", requests);
                Log.d("JSON", "requestObj: " + requestObj);

                // connection to api
                URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=" + apiKey);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                Log.d("HTTP", "conn: " + httpURLConnection);

                // send request
                OutputStream outputStream = httpURLConnection.getOutputStream();
                Log.d("OS", "os: " + outputStream);
                outputStream.write(requestObj.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                int statusCode = httpURLConnection.getResponseCode();
                Log.d("statusCode", "statusCode: " + statusCode);

                // read response
                InputStream inputStream;
                if (statusCode >= 200 && statusCode < 300) {
                    inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                } else {
                    inputStream = new BufferedInputStream(httpURLConnection.getErrorStream());
                }
                Log.d("InputStream", "in: " + inputStream);

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                Log.d("BufferedReader", "reader: " + reader);

                // create string from json response
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                JSONObject responseJson = new JSONObject(sb.toString()); // {"responses": [{"fullTextAnnotation": {"text": ""},"textAnnotations": [ ... ]}}
                Log.d("JSON", "responseJson: " + responseJson);

                // gets fullTextAnnotations object
                JSONObject response = responseJson.getJSONArray("responses").getJSONObject(0);
                Log.d("JSON", "response: " + response);

                if (response.has("fullTextAnnotation")) {
                    // get recognized text
                    String text = response.getJSONObject("fullTextAnnotation").getString("text");
                    callback.accept(text);
                } else {
                    callback.accept("Kein Text erkannt.");
                }


            } catch (Exception e) {
                e.printStackTrace();
                callback.accept("Fehler bei der Texterkennung.");
            }
        }).start();
    }
}
