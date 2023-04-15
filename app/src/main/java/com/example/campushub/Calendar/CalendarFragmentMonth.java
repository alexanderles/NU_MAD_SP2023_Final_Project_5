package com.example.campushub.Calendar;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campushub.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragmentMonth#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragmentMonth extends Fragment implements CalendarAdapter.OnCalendarItemListener {

    private static final String TAG = "Log";
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private ICalendarMonthActions monthActionsListener;
    private boolean currentMonth;
    private LocalDate today;
    private LocalDate selectedDate;
    private ImageView nextMonth;
    private ImageView prevMonth;

    public CalendarFragmentMonth() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragmentMonth.
     */
    public static CalendarFragmentMonth newInstance() {
        CalendarFragmentMonth fragment = new CalendarFragmentMonth();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar_month, container, false);

        monthYearText = rootView.findViewById(R.id.textView_month_day);
        calendarRecyclerView = rootView.findViewById(R.id.dayEventsRecyclerView);
        nextMonth = rootView.findViewById(R.id.imageView_next_day);
        prevMonth = rootView.findViewById(R.id.imageView_previous_day);
        today = LocalDate.now();
        selectedDate = LocalDate.now();
        setMonthView();

        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.plusMonths(1);
                setMonthView();
            }
        });

        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusMonths(1);
                setMonthView();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof CalendarFragmentMonth.ICalendarMonthActions) {
            monthActionsListener = (ICalendarMonthActions) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ICalendarMonthActions");
        }
    }

    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        if (selectedDate.getMonth() == today.getMonth()) {
            currentMonth = true;
        } else {
            currentMonth = false;
        }
        calendarAdapter = new CalendarAdapter(today, daysInMonth, this, currentMonth);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date)
    {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                daysInMonthArray.add("");
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return  daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals(""))
        {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            monthActionsListener.dayClicked(selectedDate);
        }
    }

    public interface ICalendarMonthActions {
        void dayClicked(LocalDate selectedDate);
    }
}