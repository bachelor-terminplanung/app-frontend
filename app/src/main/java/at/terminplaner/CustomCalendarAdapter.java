package at.terminplaner;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import java.util.Calendar;

public class CustomCalendarAdapter extends BaseAdapter {

    private Context context;
    private int year, month, desiredDay;
    private int todayYear, todayMonth, todayDay;
    private FragmentManager fragmentManager;

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
        TextView dayView;

        if (convertView == null) {
            dayView = new TextView(context);
            dayView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
            dayView.setGravity(Gravity.CENTER);
            dayView.setPadding(8, 8, 8, 8);
        } else {
            dayView = (TextView) convertView;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek + 5) % 7;

        int dayNumber = position - offset + 1;

        if (dayNumber <= 0 || dayNumber > getNumberOfDaysInMonth(year, month)) {
            dayView.setText("");
            dayView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            dayView.setText(String.valueOf(dayNumber));
            dayView.setBackgroundColor(Color.WHITE);
            dayView.setTextColor(Color.DKGRAY);

            if (year == todayYear && month == todayMonth && dayNumber == todayDay) {
                dayView.setBackgroundResource(R.drawable.circle_today);
                dayView.setTextColor(Color.BLACK);
            }

            if (desiredDay > 0 && dayNumber == desiredDay) {
                dayView.setBackgroundColor(Color.parseColor("#BF9A8E"));
                dayView.setTextColor(Color.WHITE);
            }

            dayView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("year", year);
                args.putInt("month", month);
                args.putInt("day", dayNumber);

                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(v);
                navController.navigate(R.id.action_calendarFragment_to_calendarDayViewFragment, args);
            });
        }

        return dayView;
    }

    private int getNumberOfDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
