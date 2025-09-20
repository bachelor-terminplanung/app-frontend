package at.terminplaner;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Calendar;

public class CustomCalendarAdapter extends BaseAdapter {

    private Context context;
    private int year;
    private int month;
    private int desiredDay;

    private int todayYear;
    private int todayMonth;
    private int todayDay;

    public CustomCalendarAdapter(Context context, int year, int month, int desiredDay) {
        this.context = context;
        this.year = year;
        this.month = month;
        this.desiredDay = desiredDay;

        Calendar today = Calendar.getInstance();
        todayYear = today.get(Calendar.YEAR);
        todayMonth = today.get(Calendar.MONTH);
        todayDay = today.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public int getCount() {
        return getNumberOfDaysInMonth(year, month);
    }

    @Override
    public Object getItem(int position) {
        return position + 1;
    }

    @Override
    public long getItemId(int position) {
        return position + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView dayView;

        if (convertView == null) {
            dayView = new TextView(context);
            dayView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 150
            ));
            dayView.setGravity(Gravity.CENTER);
            dayView.setPadding(8, 8, 8, 8);
        } else {
            dayView = (TextView) convertView;
        }

        int dayNumber = position + 1;
        dayView.setText(String.valueOf(dayNumber));

        // Hintergrund standardmäßig weiß
        dayView.setBackgroundColor(Color.WHITE);
        dayView.setTextColor(Color.DKGRAY);

        // Heute hervorheben
        if (year == todayYear && month == todayMonth && dayNumber == todayDay) {
            dayView.setBackgroundColor(Color.parseColor("#2196F3"));
            dayView.setTextColor(Color.BLACK);
        }

        // Optional: desiredDay hervorheben
        if (desiredDay > 0 && dayNumber == desiredDay) {
            dayView.setBackgroundColor(Color.parseColor("#BF9A8E")); // spezielle Farbe
            dayView.setTextColor(Color.WHITE);
        }

        return dayView;
    }

    private int getNumberOfDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
