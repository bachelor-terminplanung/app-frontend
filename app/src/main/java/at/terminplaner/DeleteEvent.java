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
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);

        event = getIntent().getParcelableExtra("event");

        Log.d("EVENT", "event: " + event.toString());
        event.getEventID(new EventIdCallback() {
            @Override
            public void onEventIdReceived(int eventId) {
                runOnUiThread(() -> {
                    deleteEvent(eventId, new Callback() {
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


    }

    public static void deleteEvent(int eventId, Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("event_id",eventId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + eventId)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
