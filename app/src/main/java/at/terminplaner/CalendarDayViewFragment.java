package at.terminplaner;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import at.terminplaner.databinding.FragmentCalendarDayViewBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalendarDayViewFragment extends Fragment {
    String IP_Address = "192.168.10.28";
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    private int year, month, day;
    private FragmentCalendarDayViewBinding binding;
    private Map<String, LinearLayout> slotMap = new HashMap<>();
    private OkHttpClient client = new OkHttpClient();

    public static CalendarDayViewFragment newInstance(int year, int month, int day) {
        CalendarDayViewFragment fragment = new CalendarDayViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            year = getArguments().getInt(ARG_YEAR);
            month = getArguments().getInt(ARG_MONTH);
            day = getArguments().getInt(ARG_DAY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarDayViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBackToCalendar.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        //Datum anzeigen
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        String dayName = new SimpleDateFormat("EEEE", Locale.GERMAN).format(calendar.getTime());
        String dateString = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year);
        binding.dayHeaderTextView.setText(dayName + ", " + dateString);

        //Uhrzeit-Slots
        String[] hours = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        LinearLayout timeSlotsLayout = binding.timeSlotsLayout;

        for (String hour : hours) {
            TextView textView = new TextView(requireContext());
            textView.setText(hour);
            textView.setTextSize(18f);
            textView.setPadding(0, 8, 0, 4);
            timeSlotsLayout.addView(textView);

            View line = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2
            );
            line.setLayoutParams(params);
            line.setBackgroundColor(0xFF000000);
            timeSlotsLayout.addView(line);

            LinearLayout eventsContainer = new LinearLayout(requireContext());
            eventsContainer.setOrientation(LinearLayout.VERTICAL);
            eventsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            timeSlotsLayout.addView(eventsContainer);

            slotMap.put(hour, eventsContainer);
        }

        String apiDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        loadEventsForDay(apiDate);
    }

    private void addEventToSlot(Event event, String userColor) {
        //Event horizontal layout
        LinearLayout eventLayout = new LinearLayout(requireContext());
        eventLayout.setOrientation(LinearLayout.HORIZONTAL);
        eventLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams eventLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        eventLayoutParams.setMargins(0, 4, 0, 4);
        eventLayout.setLayoutParams(eventLayoutParams);

        //Farbkreis
        View circle = new View(requireContext());
        int sizeInPx = (int) (16 * getResources().getDisplayMetrics().density); // 16dp
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        circleParams.setMargins(0, 0, 8, 0);
        circle.setLayoutParams(circleParams);
        circle.setBackgroundResource(R.drawable.circle_event_dayview);
        try {
            circle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(userColor)));
        } catch (Exception e) {
            circle.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        }

        //Beschreibung
        TextView description = new TextView(requireContext());
        description.setText(event.getDescription());
        description.setTextSize(16f);
        description.setTextColor(Color.BLACK);

        //Event zusammensetzen
        eventLayout.addView(circle);
        eventLayout.addView(description);

        //Detailansicht öffnen
        eventLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DetailedCalendar.class);
            intent.putExtra("event", event);
            startActivity(intent);
        });

        String slotKey = event.getTime().split(":")[0] + ":00"; //volle Stunde
        LinearLayout slotContainer = slotMap.getOrDefault(slotKey, binding.dayEventsContainer);
        slotContainer.addView(eventLayout);
    }

    private void loadEventsForDay(String date) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://" + IP_Address + ":3000/event/date";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("date", date);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Fehler beim Laden der Events", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("LOAD_EVENTS", "Response Fehler: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                Log.d("LOAD_EVENTS", "Response: " + responseBody);

                try {
                    JSONArray eventsArray = new JSONArray(responseBody);

                    Calendar selectedDay = Calendar.getInstance();
                    String[] dateParts = date.split("-");
                    selectedDay.set(Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]) - 1,
                            Integer.parseInt(dateParts[2]));

                    for (int i = 0; i < eventsArray.length(); i++) {
                        JSONObject obj = eventsArray.getJSONObject(i);

                        String eventDate = obj.optString("event_date", null);
                        String startTime = obj.optString("start_time", "00:00");
                        String descriptionText = obj.optString("description", "");
                        int userId = obj.optInt("user_id", -1);
                        boolean isRepeating = obj.optInt("is_repeating", 0) != 0;
                        String repeatType = obj.optString("repeat_type", null);
                        String repeatUntil = obj.optString("repeat_until", null);

                        // Prüfen, ob Event auf diesen Tag fällt
                        boolean shouldShow = false;

                        if (!isRepeating) {
                            shouldShow = date.equals(eventDate);
                        } else {
                            Calendar eventDay = Calendar.getInstance();
                            if (eventDate != null && !eventDate.equals("null")) {
                                String[] parts = eventDate.split("-");
                                eventDay.set(Integer.parseInt(parts[0]),
                                        Integer.parseInt(parts[1]) - 1,
                                        Integer.parseInt(parts[2]));
                            }

                            Calendar repeatUntilCal = null;
                            if (repeatUntil != null && !repeatUntil.equals("null") && !repeatUntil.equals("0000-00-00")) {
                                String[] parts = repeatUntil.split("-");
                                repeatUntilCal = Calendar.getInstance();
                                repeatUntilCal.set(Integer.parseInt(parts[0]),
                                        Integer.parseInt(parts[1]) - 1,
                                        Integer.parseInt(parts[2]));
                            }

                            switch (repeatType != null ? repeatType.toLowerCase(Locale.ROOT) : "") {
                                case "täglich":
                                    shouldShow = !selectedDay.before(eventDay)
                                            && (repeatUntilCal == null || !selectedDay.after(repeatUntilCal));
                                    break;
                                case "wöchentlich":
                                    shouldShow = !selectedDay.before(eventDay)
                                            && selectedDay.get(Calendar.DAY_OF_WEEK) == eventDay.get(Calendar.DAY_OF_WEEK)
                                            && (repeatUntilCal == null || !selectedDay.after(repeatUntilCal));
                                    break;
                                case "monatlich":
                                    shouldShow = !selectedDay.before(eventDay)
                                            && selectedDay.get(Calendar.DAY_OF_MONTH) == eventDay.get(Calendar.DAY_OF_MONTH)
                                            && (repeatUntilCal == null || !selectedDay.after(repeatUntilCal));
                                    break;
                                case "jährlich":
                                    shouldShow = !selectedDay.before(eventDay)
                                            && selectedDay.get(Calendar.DAY_OF_MONTH) == eventDay.get(Calendar.DAY_OF_MONTH)
                                            && selectedDay.get(Calendar.MONTH) == eventDay.get(Calendar.MONTH)
                                            && (repeatUntilCal == null || !selectedDay.after(repeatUntilCal));
                                    break;
                                default:
                                    shouldShow = date.equals(eventDate);
                            }
                        }

                        if (!shouldShow) continue;

                        Event event = new Event(descriptionText, eventDate, startTime,
                                0, isRepeating, repeatType, repeatUntil);

                        // User-Farbe laden
                        if (userId != -1) {
                            User.getColorById(userId, new User.ColorCallback() {
                                @Override
                                public void onColorReceived(String color) {
                                    requireActivity().runOnUiThread(() -> addEventToSlot(event, color));
                                }

                                @Override
                                public void onError(String message) {
                                    requireActivity().runOnUiThread(() -> addEventToSlot(event, "#2196F3"));
                                }
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> addEventToSlot(event, "#2196F3"));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
