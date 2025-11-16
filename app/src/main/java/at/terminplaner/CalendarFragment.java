package at.terminplaner;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {
    String IP_Address = "192.168.10.28";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageButton ocrButton;
    private Button detailButton;
    private RelativeLayout calendarBackground;

    public CalendarFragment() {

    }

    private GridView gridView;
    private TextView monthYearText;
    private ImageButton prevMonth;
    private ImageButton nextMonth;
    private ImageButton logoutButton;
    private int year;
    private int month;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ocrButton = view.findViewById(R.id.addEvent);
        logoutButton = view.findViewById(R.id.logoutButton);

        ocrButton.setOnClickListener(v -> {
            int userId = ((MyApp) requireActivity().getApplication()).getUserId();

            if (userId == 0) {
                Toast.makeText(getContext(), "Benutzer-ID noch nicht erkannt", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), CloudOCR.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(CalendarFragment.this)
                    .navigate(R.id.action_calendarFragment_to_loginFragment);
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        gridView = view.findViewById(R.id.gridView);
        monthYearText = view.findViewById(R.id.monthYearText);
        prevMonth = view.findViewById(R.id.prevMonth);
        nextMonth = view.findViewById(R.id.nextMonth);

        calendarBackground = view.findViewById(R.id.calendarBackground);

        Calendar today = Calendar.getInstance();
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);

        updateCalendar();

        prevMonth.setOnClickListener(v -> {
            month--;
            if (month < 0) {
                month = 11;
                year--;
            }
            updateCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            month++;
            if (month > 11) {
                month = 0;
                year++;
            }
            updateCalendar();
        });

        return view;
    }

    private void updateCalendar() {
        String start = year + "-" + String.format("%02d", month + 1) + "-01";
        String end = year + "-" + String.format("%02d", month + 1) + "-" + getLastDayOfMonth(year, month);
        setCalendarBackground(month);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("start", start);
            jsonBody.put("end", end);
        } catch (JSONException e) { e.printStackTrace(); }

        String url = "http://" + IP_Address + ":3000/event/range";
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONArray events = new JSONArray(responseBody);
                    Set<Integer> daysWithEvents = new HashSet<>();
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject obj = events.getJSONObject(i);
                        String date = obj.getString("event_date");
                        int day = Integer.parseInt(date.split("-")[2]);
                        daysWithEvents.add(day);
                    }

                    requireActivity().runOnUiThread(() -> {
                        CustomCalendarAdapter adapter = new CustomCalendarAdapter(requireContext(), year, month, -1, getParentFragmentManager());
                        adapter.setDaysWithEvents(daysWithEvents);
                        gridView.setAdapter(adapter);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //Monat-Jahr-Text
        String monthName = new DateFormatSymbols().getMonths()[month];

        monthYearText.setText(monthName + " " + year);
    }

    private void setCalendarBackground(int month) {
        Log.d("month", "setCalendarBackground: " + month);
        if (month >= 2 && month <= 4) {
            calendarBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spring));
        } else if (month >= 5 && month <= 7) {
            calendarBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sommer));
        } else if (month >= 8 && month <= 10) {
            calendarBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.herbst));
        } else {
            calendarBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.winter));
        }
    }

    private int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        HashMap<Object, Properties> descHashMap = new HashMap<>();
        Properties usedProperty = new Properties();
        RecyclerView.LayoutManager.Properties used = new RecyclerView.LayoutManager.Properties();

    }
}