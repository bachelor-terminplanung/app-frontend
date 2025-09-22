package at.terminplaner;

import static at.terminplaner.UpdateEvent.parseDurationToMinutes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailedCalendar extends AppCompatActivity {

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);

        // Event aus Intent laden
        event = getIntent().getParcelableExtra("event");

        // Views
        Button deleteButton = findViewById(R.id.deleteButton);
        Button updateButton = findViewById(R.id.updateButton);
        TextView date = findViewById(R.id.detailedViewDate);
        TextView time = findViewById(R.id.detailedViewTime);
        TextView description = findViewById(R.id.detailedViewDescription);
        TextView duration = findViewById(R.id.detailedViewDuration);
        TextView isRepeating = findViewById(R.id.detailedViewIsRepeating);
        TextView repeatType = findViewById(R.id.detailedViewRepeatType);
        TextView repeatUntil = findViewById(R.id.detailedViewRepeatUntil);

        // Event-Werte setzen
        if (event != null) {
            date.setText(event.getDate());
            time.setText(event.getTime());
            description.setText(event.getDescription());
            duration.setText(String.valueOf(event.getDuration()));
            isRepeating.setText(String.valueOf(event.isRepeating()));
            repeatType.setText(event.getRepeatType() != null ? event.getRepeatType() : "");
            repeatUntil.setText(event.getRepeatUntil() != null ? event.getRepeatUntil() : "");
        }

        // Update-Button -> Fragment öffnen
        updateButton.setOnClickListener(v -> {
            UpdateEventFragment fragment = UpdateEventFragment.newInstance(event);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.loadingOverlay, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Delete-Button -> Fragment öffnen
        deleteButton.setOnClickListener(v -> {
            DeleteEventFragment fragment = DeleteEventFragment.newInstance(event);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
