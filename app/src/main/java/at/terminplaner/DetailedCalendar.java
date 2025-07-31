package at.terminplaner;

import static at.terminplaner.UpdateEvent.parseDurationToMinutes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailedCalendar extends AppCompatActivity {
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);

        event = getIntent().getParcelableExtra("event");

        if (event == null) {
            Log.e("DetailedViewActivity", "Kein Event empfangen!");
            finish();
            return;
        }

        Button deleteButton = findViewById(R.id.deleteButton);
        Button updateButton = findViewById(R.id.updateButton);
        TextView date = findViewById(R.id.detailedViewDate);
        TextView time = findViewById(R.id.detailedViewTime);
        TextView description = findViewById(R.id.detailedViewDescription);
        TextView duration = findViewById(R.id.detailedViewDuration);
        TextView isRepeating = findViewById(R.id.detailedViewIsRepeating);
        TextView repeatType = findViewById(R.id.detailedViewRepeatType);
        TextView repeatUntil = findViewById(R.id.detailedViewRepeatUntil);

        event.date = String.valueOf(date.getText());
        event.time = (String) time.getText();
        event.description = (String) description.getText();
        event.duration = parseDurationToMinutes(duration.getText().toString().trim());
        event.isRepeating = Boolean.parseBoolean(isRepeating.getText().toString().trim());
        event.repeatType = (String) repeatType.getText();
        event.repeatUntil = (String) repeatUntil.getText();


        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateEvent.class);
            intent.putExtra("event", event);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeleteEvent.class);
            intent.putExtra("event", event);
            startActivity(intent);
        });
    }
}
