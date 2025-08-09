package at.terminplaner;

import static at.terminplaner.UpdateEvent.parseDurationToMinutes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailedCalendar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_view);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button updateButton = findViewById(R.id.updateButton);
        TextView date = findViewById(R.id.detailedViewDate);
        TextView time = findViewById(R.id.detailedViewTime);
        TextView description = findViewById(R.id.detailedViewDescription);
        TextView duration = findViewById(R.id.detailedViewDuration);
        TextView isRepeating = findViewById(R.id.detailedViewIsRepeating);
        TextView repeatType = findViewById(R.id.detailedViewRepeatType);
        TextView repeatUntil = findViewById(R.id.detailedViewRepeatUntil);

        updateButton.setOnClickListener(v -> {
            Event event = new Event(
                    description.getText().toString(),
                    date.getText().toString(),
                    time.getText().toString(),
                    parseDurationToMinutes(duration.getText().toString().trim()),
                    Boolean.parseBoolean(isRepeating.getText().toString().trim()),
                    repeatType.getText().toString(),
                    repeatUntil.getText().toString()
            );

            UpdateEventFragment fragment = UpdateEventFragment.newInstance(event);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.loadingOverlay, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        deleteButton.setOnClickListener(v -> {
            String dateS = date.getText().toString();
            String timeS = time.getText().toString();
            String descriptionS = description.getText().toString();
            int durationS = parseDurationToMinutes(duration.getText().toString().trim());
            boolean isRepeatingS = Boolean.parseBoolean(isRepeating.getText().toString().trim());
            String repeatTypeS = repeatType.getText().toString();
            String repeatUntilS = repeatUntil.getText().toString();

            Event event = new Event(
                    descriptionS,
                    dateS,
                    timeS,
                    durationS,
                    isRepeatingS,
                    repeatTypeS,
                    repeatUntilS
            );

            // show fragment
            DeleteEventFragment fragment = DeleteEventFragment.newInstance(event);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void startIntent(TextView date, TextView time, TextView description, TextView duration, TextView isRepeating, TextView repeatType, TextView repeatUntil, Class intentReceiverClass) {
        String dateS = String.valueOf(date.getText());
        String timeS = (String) time.getText();
        String descriptionS = (String) description.getText();
        int durationS = parseDurationToMinutes(duration.getText().toString().trim());
        boolean isRepeatingS = Boolean.parseBoolean(isRepeating.getText().toString().trim());
        String repeatTypeS = (String) repeatType.getText();
        String repeatUntilS = (String) repeatUntil.getText();

        Event updatedEvent = new Event(
                descriptionS,
                dateS,
                timeS,
                durationS,
                isRepeatingS,
                repeatTypeS,
                repeatUntilS
        );
        Log.d("EVENT", "event delete: " + updatedEvent.toString());

        Intent intent = new Intent(this, intentReceiverClass);
        intent.putExtra("event", updatedEvent);
        startActivity(intent);
    }
}
