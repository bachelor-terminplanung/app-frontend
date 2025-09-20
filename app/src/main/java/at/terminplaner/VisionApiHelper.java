package at.terminplaner;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
import java.util.List;
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

    public static void sendEvent(CloudOCR cloudOCR, String description, String date, String time, int duration, boolean isRepeating, String repeatType, String repeatUntil) {
        try {
            URL url = new URL("10.0.2.2:3000/event");
            int userId = ((MyApp) cloudOCR.getApplication()).getUserId();

            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("event_date", date);
            json.put("start_time", time);
            json.put("description", description);
            json.put("duration", duration);
            json.put("is_repeating", isRepeating);
            json.put("repeat_type", repeatType.isEmpty() ? JSONObject.NULL : repeatType);
            json.put("repeat_until", repeatUntil.isEmpty() ? JSONObject.NULL : repeatUntil);

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
                cloudOCR.runOnUiThread(() ->
                        Toast.makeText(cloudOCR, "Termin erfolgreich gespeichert!", Toast.LENGTH_SHORT).show()
                );
                System.out.println("Event erfolgreich gesendet.");
            } else {
                cloudOCR.runOnUiThread(() ->
                        Toast.makeText(cloudOCR, "Termin konnte nicht erfolgreich gespeichert werden!", Toast.LENGTH_SHORT).show()
                );
                System.out.println("Fehler beim Senden des Events. HTTP-Code: " + responseCode);
            }

            httpURLConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recognizeHandwriting(Bitmap bitmap, String apiKey, Consumer<String> callback, CloudOCR cloudOCR) {
        new Thread(() -> {
            try {
                JSONObject requestObj =  preparePayload(bitmap);
                String detectedText = getTextFromGoogleVisionApi(requestObj, apiKey);

                List<String> dates = DateTimeRecognizer.getRecognicedDatesOrTimes(detectedText, DateTimeRecognizer.Type.DATE);
                List<String> times = DateTimeRecognizer.getRecognicedDatesOrTimes(detectedText, DateTimeRecognizer.Type.TIME);
                Log.d("dates",dates.isEmpty() ? "" : dates.get(0));
                Log.d("times", times.isEmpty() ? "" : times.get(0));
                String date = dates.isEmpty() ? "" : dates.get(0);
                String time = times.isEmpty() ? "" : times.get(0);
                Event event = new Event(detectedText,date, time, 0,false, "","");
                cloudOCR.runOnUiThread(() -> EventPopUp.showDetailedPopup(cloudOCR, event, true, false, inputEvent -> {
                    // callback ?
                    cloudOCR.checkAndHandleDuplicate(inputEvent);

                }));
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

