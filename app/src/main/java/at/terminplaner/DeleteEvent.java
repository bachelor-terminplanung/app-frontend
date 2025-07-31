package at.terminplaner;

import static at.terminplaner.UpdateEvent.parseDurationToMinutes;

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

public class DeleteEvent extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.10.28:3000/event";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);

        Button deleteButton = findViewById(R.id.deleteButton);
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

        deleteButton.setOnClickListener(v -> {
            Event event = new Event(descriptionS, dateS ,timeS, durationS, isRepeatingS, repeatTypeS, repeatUntilS);
            Log.d("EVENT", "event: " + event.toString());
            event.getEventID(new EventIdCallback() {
                @Override
                public void onEventIdReceived(int eventId) {
                    runOnUiThread(() -> {
                        deleteEvent(event, eventId, new Callback() {
                            @Override
                            public void onFailure(okhttp3.Call call, IOException e) {
                                runOnUiThread(() ->
                                        Toast.makeText(DeleteEvent.this, "Löschen fehlgeschlagen", Toast.LENGTH_SHORT).show()
                                );
                            }

                            @Override
                            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                runOnUiThread(() ->
                                        Toast.makeText(DeleteEvent.this, "Event gelöscht", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(DeleteEvent.this, "Fehler beim Finden des Events: " + errorMessage, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

    }
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
