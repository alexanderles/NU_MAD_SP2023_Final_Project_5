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

import com.example.campushub.Events.Event;
import com.example.campushub.Events.EventComparator;
import com.example.campushub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Fragment for Calendar Month View
 *
 * Shows the days of the month with dots for days that have events that the
 * user has registered for
 *
 * Credit to "CodeWithCal" (https://www.youtube.com/@CodeWithCal) for calendar
 * design inspiration
 */
public class CalendarFragmentMonth extends Fragment implements CalendarAdapter.OnCalendarItemListener {

    private static final String TAG = "Log";
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private ICalendarMonthActions monthActionsListener;
    private boolean isCurrentMonth;
    private LocalDate today;
    private LocalDate selectedDate;
    private ImageView nextMonth;
    private ImageView prevMonth;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
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
                loadEvents();
            }
        });

        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusMonths(1);
                setMonthView();
                loadEvents();
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

    private void loadEvents() {
        ArrayList<Event> events = new ArrayList<>();

        db.collection("Member_Users")
                .document(mUser.getEmail())
                .collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String eventReference = document.get("eventId").toString();
                                db.collection("events")
                                        .document(eventReference)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot snap = task.getResult();
                                                    Object potentialOrgImage = snap.get("eventOrganizerImage");
                                                    String eventOrgImage = (potentialOrgImage == null) ?
                                                            null : potentialOrgImage.toString();
                                                    Event newEvent = new Event(
                                                            eventReference,
                                                            snap.get("eventName").toString(),
                                                            snap.get("eventOwnerName").toString(),
                                                            snap.get("eventOwnerEmail").toString(),
                                                            eventOrgImage,
                                                            snap.get("eventLocation").toString(),
                                                            snap.get("eventTime").toString(),
                                                            snap.get("eventDescription").toString()
                                                    );
                                                    events.add(newEvent);
                                                    updateRecyclerView(events);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public void updateRecyclerView(ArrayList<Event> events){
        events.sort(new EventComparator());
        calendarAdapter.setEvents(events);
        calendarAdapter.notifyDataSetChanged();
    }

    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        isCurrentMonth = selectedDate.getMonth() == today.getMonth();
        calendarAdapter = new CalendarAdapter(today, daysInMonth, this, isCurrentMonth, selectedDate);
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
            int clickedDayNumber = Integer.parseInt(dayText);
            int selectedDayNumber = selectedDate.getDayOfMonth();
            int difference = Math.abs(selectedDayNumber - clickedDayNumber);

            if (selectedDayNumber >= clickedDayNumber) {
                selectedDate = selectedDate.minusDays(difference);
            } else {
                selectedDate = selectedDate.plusDays(difference);
            }
            monthActionsListener.dayClicked(selectedDate);
        }
    }

    public interface ICalendarMonthActions {
        void dayClicked(LocalDate selectedDate);
    }
}