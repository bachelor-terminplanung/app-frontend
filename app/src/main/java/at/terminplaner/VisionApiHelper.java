package at.terminplaner;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class VisionApiHelper {

    private static JSONObject preparePayload(Bitmap bitmap) {
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
            return requestObj;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
    private static String getTextFromGoogleVisionApi(JSONObject requestObj, String apiKey) {
        try {
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
                return text;
                //callback.accept(text);
            } else {
                //callback.accept("Kein Text erkannt.");
            }
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void sendEvent(String description) {
        try {
            URL url = new URL("http://192.168.10.28:3000/events");

            // JSON-Objekt erstellen
            JSONObject json = new JSONObject();
            json.put("user_id", 1);
            json.put("event_date", "2025-07-13");
            json.put("start_time", "10:00");
            json.put("description", description);
            json.put("duration", 60);
            json.put("is_repeating", false);
            json.put("repeat_type", JSONObject.NULL);
            json.put("repeat_until", JSONObject.NULL);
            json.put("reminder_at", "2025-07-13T09:00:00");


            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);
            Log.d("HTTP", "conn: " + httpURLConnection);

            // send request
            OutputStream outputStream = httpURLConnection.getOutputStream();
            Log.d("OS", "os: " + outputStream);
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
            outputStream.close();


            int responseCode = httpURLConnection.getResponseCode();
            Log.d("responseCode", "responseCode: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Event erfolgreich gesendet.");
            } else {
                System.out.println("Fehler beim Senden des Events. HTTP-Code: " + responseCode);
            }

            httpURLConnection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void recognizeHandwriting(Bitmap bitmap, String apiKey, Consumer<String> callback) {
        new Thread(() -> {
            try {
                JSONObject requestObj =  preparePayload(bitmap);
                String detectedText = getTextFromGoogleVisionApi(requestObj, apiKey);

                sendEvent(detectedText);

                if (detectedText != null) {
                    callback.accept(detectedText);
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
