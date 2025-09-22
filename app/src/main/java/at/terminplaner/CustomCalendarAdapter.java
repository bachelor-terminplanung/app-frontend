package at.terminplaner;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CustomCalendarAdapter extends BaseAdapter {

    private Context context;
    private int year, month, desiredDay;
    private int todayYear, todayMonth, todayDay;
    private FragmentManager fragmentManager;

    private Set<Integer> daysWithEvents = new HashSet<>();

    public CustomCalendarAdapter(Context context, int year, int month, int desiredDay, FragmentManager fm) {
        this.context = context;
        this.year = year;
        this.month = month;
        this.desiredDay = desiredDay;
        this.fragmentManager = fm;

        Calendar today = Calendar.getInstance();
        todayYear = today.get(Calendar.YEAR);
        todayMonth = today.get(Calendar.MONTH);
        todayDay = today.get(Calendar.DAY_OF_MONTH);
    }

    public void setDaysWithEvents(Set<Integer> daysWithEvents) {
        this.daysWithEvents = daysWithEvents;
    }

    @Override
    public int getCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek + 5) % 7; // Montag=0
        return getNumberOfDaysInMonth(year, month) + offset;
    }

    @Override
    public Object getItem(int position) { return position; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout dayLayout = new RelativeLayout(context);
        dayLayout.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));

        TextView dayView = new TextView(context);
        dayView.setGravity(Gravity.CENTER);
        dayView.setPadding(8, 8, 8, 8);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek + 5) % 7;
        int dayNumber = position - offset + 1;

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        if (dayNumber <= 0 || dayNumber > getNumberOfDaysInMonth(year, month)) {
            dayView.setText("");
            dayLayout.setBackgroundColor(Color.TRANSPARENT);
        } else {
            dayView.setText(String.valueOf(dayNumber));
            dayView.setTextColor(Color.DKGRAY);
            dayLayout.setBackgroundColor(Color.WHITE);

            // Heute markieren
            if (year == todayYear && month == todayMonth && dayNumber == todayDay) {
                dayLayout.setBackgroundResource(R.drawable.circle_today);
                dayView.setTextColor(Color.BLACK);
            }

            // Gewünschter Tag
            if (desiredDay > 0 && dayNumber == desiredDay) {
                dayLayout.setBackgroundColor(Color.parseColor("#BF9A8E"));
                dayView.setTextColor(Color.WHITE);
            }

            // Klick auf Tag
            int finalDayNumber = dayNumber;
            dayLayout.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("year", year);
                args.putInt("month", month);
                args.putInt("day", finalDayNumber);

                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(v);
                navController.navigate(R.id.action_calendarFragment_to_calendarDayViewFragment, args);
            });

            // Punkt für Events
            if (daysWithEvents.contains(dayNumber)) {
                View dot = new View(context);
                dot.setBackgroundResource(R.drawable.circle_event_calendar);
                RelativeLayout.LayoutParams dotParams = new RelativeLayout.LayoutParams(20, 20);
                dotParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                dotParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                dotParams.bottomMargin = 8;
                dayLayout.addView(dot, dotParams);
            }
        }

        dayLayout.addView(dayView, textParams);
        return dayLayout;
    }

    private int getNumberOfDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
