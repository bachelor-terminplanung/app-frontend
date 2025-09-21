package at.terminplaner;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.Properties;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button ocrButton;
    private Button detailButton;

    public CalendarFragment() {
        // Required empty public constructor
    }

    private GridView gridView;
    private TextView monthYearText;
    private Button prevMonth;
    private Button nextMonth;
    private Button logoutButton;
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
        detailButton = view.findViewById(R.id.detailButton);
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
        detailButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DetailedCalendar.class);
            startActivity(intent);
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
        CustomCalendarAdapter adapter = new CustomCalendarAdapter(requireContext(), year, month, -1, getParentFragmentManager()
        );
        gridView.setAdapter(adapter);


        String monthName = new DateFormatSymbols().getMonths()[month];
        monthYearText.setText(monthName + " " + year);
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