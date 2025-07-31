package at.terminplaner;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UpdateEvent extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.10.28:3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);

        Button updateButton = findViewById(R.id.updateButton);
        TextView date = findViewById(R.id.detailedViewDate);
        TextView time = findViewById(R.id.detailedViewTime);
        TextView description = findViewById(R.id.detailedViewDescription);
        TextView duration = findViewById(R.id.detailedViewDuration);
        TextView isRepeating = findViewById(R.id.detailedViewIsRepeating);
        TextView repeatType = findViewById(R.id.detailedViewRepeatType);
        TextView repeatUntil = findViewById(R.id.detailedViewRepeatUntil);

        String dateS = String.valueOf(date.getText());
        String timeS = (String) time.getText();
        String descriptionS = (String) description.getText();
        int durationS = parseDurationToMinutes(duration.getText().toString().trim());
        boolean isRepeatingS = Boolean.parseBoolean(isRepeating.getText().toString().trim());
        String repeatTypeS = (String) repeatType.getText();
        String repeatUntilS = (String) repeatUntil.getText();

        updateButton.setOnClickListener(v -> {
            Event event = new Event(descriptionS, dateS ,timeS, durationS, isRepeatingS, repeatTypeS, repeatUntilS);
            Log.d("EVENT", "event: " + event.toString());
            getEventID(event, new EventIdCallback() {
                @Override
                public void onEventIdReceived(int eventId) {
                    runOnUiThread(() -> {
                        EventPopUp.showDetailedPopup(UpdateEvent.this, event, false, true, inputEvent -> {
                            updateEvent(inputEvent, eventId, new Callback() {
                                @Override
                                public void onFailure(okhttp3.Call call, IOException e) {
                                    runOnUiThread(() ->
                                            Toast.makeText(UpdateEvent.this, "Update fehlgeschlagen", Toast.LENGTH_SHORT).show()
                                    );
                                }

                                @Override
                                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                    runOnUiThread(() ->
                                            Toast.makeText(UpdateEvent.this, "Event aktualisiert", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            });
                        });
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(UpdateEvent.this, "Fehler beim Finden des Events: " + errorMessage, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

    }
    public static int parseDurationToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }


    public static void updateEvent(Event event, int eventId,Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("event_id", eventId);
            jsonBody.put("user_id", 1);
            jsonBody.put("event_date", event.getDate());
            jsonBody.put("start_time", event.getTime());
            jsonBody.put("description", event.getDescription());
            jsonBody.put("duration", event.getDuration());
            jsonBody.put("is_repeating", event.isRepeating());
            jsonBody.put("repeat_type", event.getRepeatType());
            if (event.getRepeatUntil() == null || event.getRepeatUntil().isEmpty()) {
                jsonBody.put("repeat_until", JSONObject.NULL);
            } else {
                jsonBody.put("repeat_until", event.getRepeatUntil());
            }        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + eventId)
                .put(body)
                .build();

        Log.d("UPDATE", "updateEvent: " + request);
        Log.d("UPDATE", "body: " + jsonBody.toString());

        client.newCall(request).enqueue(callback);
    }
    public static void getEventID(Event event, EventIdCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", 1); //
            jsonBody.put("event_date", event.getDate());
            jsonBody.put("start_time", event.getTime());
            jsonBody.put("description", event.getDescription());
            jsonBody.put("duration", event.getDuration());
            jsonBody.put("is_repeating", event.isRepeating());
            jsonBody.put("repeat_type", event.getRepeatType());
            if (event.getRepeatUntil() == null || event.getRepeatUntil().isEmpty()) {
                jsonBody.put("repeat_until", "");
            } else {
                jsonBody.put("repeat_until", event.getRepeatUntil());
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
