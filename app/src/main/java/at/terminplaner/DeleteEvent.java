package at.terminplaner;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DeleteEvent {
    private static final String BASE_URL = "http://192.168.10.28:3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static void deleteEvent(Event event, int eventId, Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("event_id",eventId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + eventId)
                .delete(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
