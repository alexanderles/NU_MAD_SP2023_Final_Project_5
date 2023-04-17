package com.example.campushub.Calendar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.campushub.R;

import org.w3c.dom.Text;

import java.time.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragmentDay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragmentDay extends Fragment {

    private static final String ARG_DATE = "date";
    private LocalDate selectedDate;
    private TextView textViewDay;
    private ImageView nextDay;
    private ImageView prevDay;
    private RecyclerView eventRecyclerView;

    public CalendarFragmentDay() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragmentDay.
     */
    public static CalendarFragmentDay newInstance(LocalDate date) {
        CalendarFragmentDay fragment = new CalendarFragmentDay();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = (LocalDate) getArguments().getSerializable(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar_day, container, false);

        nextDay = rootView.findViewById(R.id.imageView_next_day);
        prevDay = rootView.findViewById(R.id.imageView_previous_day);
        textViewDay = rootView.findViewById(R.id.textView_month_day);

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.plusDays(1);
                setEventsView();
            }
        });

        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusDays(1);
                setEventsView();
            }
        });

        return rootView;
    }

    private void setEventsView() {

    }
}