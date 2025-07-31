package at.terminplaner;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class DuplicateEventChecker {
    private static final String BASE_URL = "http://192.168.10.28:3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void checkDuplicateEvent(Context context, String date, String time, String description, DuplicateCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("event_date", date);
            jsonBody.put("start_time", time);
            jsonBody.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                JSON
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/duplicate-check")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onError("Verbindungsfehler");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        boolean isDuplicate = json.getBoolean("isDuplicate");
                        int count = json.getInt("count");
                        callback.onResult(isDuplicate, count);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError("Fehler beim Lesen der Daten.");
                    }
                } else {
                    callback.onError("Serverfehler: " + response.code());
                }
            }
        });
    }

    public interface DuplicateCallback {
        void onResult(boolean isDuplicate, int count);
        void onError(String errorMessage);
    }
}
