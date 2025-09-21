package at.terminplaner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import at.terminplaner.databinding.FragmentCalendarDayViewBinding;

public class CalendarDayViewFragment extends Fragment {

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    private int year, month, day;
    private FragmentCalendarDayViewBinding fragmentCalendarDayViewBinding;

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
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
            day = getArguments().getInt("day");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCalendarDayViewBinding = FragmentCalendarDayViewBinding.inflate(inflater, container, false);
        return fragmentCalendarDayViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentCalendarDayViewBinding.buttonBackToCalendar.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager().popBackStack());

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        String dayName = new SimpleDateFormat("EEEE", Locale.GERMAN).format(calendar.getTime());
        String dateString = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year);
        fragmentCalendarDayViewBinding.dayHeaderTextView.setText(dayName + ", " + dateString);

        String[] hours = {"06:00","08:00","10:00","12:00","14:00","16:00","18:00","20:00","22:00"};
        LinearLayout timeSlotsLayout = fragmentCalendarDayViewBinding.timeSlotsLayout;

        for (String hour : hours) {
            TextView textView = new TextView(requireContext());
            textView.setText(hour);
            textView.setTextSize(18f);
            textView.setPadding(0, 8, 0, 4);
            timeSlotsLayout.addView(textView);

            View line = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            );
            line.setLayoutParams(params);
            line.setBackgroundColor(0xFF000000);
            timeSlotsLayout.addView(line);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentCalendarDayViewBinding = null;
    }
}
