package at.terminplaner;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Event {
    public String description;
    public String date;
    public String time;
    public int duration;
    public boolean isRepeating;
    public String repeatType;
    public String repeatUntil;

    public Event(String description, String date, String time, int duration, boolean isRepeating, String repeatType, String repeatUntil) {
        this.description = description;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.isRepeating = isRepeating;
        this.repeatType = repeatType;
        this.repeatUntil = repeatUntil;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRepeatUntil() {
        return repeatUntil;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public String getRepeatType() {
        return repeatType;
    }

    @Override
    public String toString() {
        return "Event{" +
                "description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", duration=" + duration +
                ", isRepeating=" + isRepeating +
                ", repeatType='" + repeatType + '\'' +
                ", repeatUntil='" + repeatUntil + '\'' +
                '}';
    }


    public  void getEventID(EventIdCallback callback) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String BASE_URL = "http://192.168.10.28:3000/event";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", 1); //
            jsonBody.put("event_date", getDate());
            jsonBody.put("start_time", getTime());
            jsonBody.put("description", getDescription());
            jsonBody.put("duration", getDuration());
            jsonBody.put("is_repeating", isRepeating());
            jsonBody.put("repeat_type",getRepeatType());
            if (getRepeatUntil() == null || getRepeatUntil().isEmpty()) {
                jsonBody.put("repeat_until", "");
            } else {
                jsonBody.put("repeat_until", getRepeatUntil());
            }
        } catch (JSONException e) {
            callback.onError("Fehler beim Erstellen des JSON-Objekts");
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Log.d("EVENT-BODY", "request body: " + jsonBody.toString());
        Request request = new Request.Builder()
                .url(BASE_URL + "/match")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("EVENT-ID", "error: " );
                callback.onError("Verbindungsfehler: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("EVENT-ID", "ResponseBody: " + responseBody);
                if (!response.isSuccessful()) {
                    callback.onError("Fehler: " + response.code());
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    int id = json.getInt("eventId");
                    Log.d("EVENT-ID", "id: " + id);
                    callback.onEventIdReceived(id);
                } catch (JSONException e) {
                    callback.onError("Ungültige Serverantwort");
                }
            }
        });
    }

}
