package com.example.campushub.Calendar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.campushub.Event;
import com.example.campushub.EventComparator;
import com.example.campushub.EventsAdapter;
import com.example.campushub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragmentDay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragmentDay extends Fragment {

    private static final String ARG_DATE = "date";
    private static final String ARG_EVENT = "events";
    private LocalDate selectedDate;
    private TextView textViewDay;
    private ImageView nextDay;
    private ImageView prevDay;
    private TextView textViewNoEvents;
    private RecyclerView recyclerView;
    private EventsAdapter eventsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ArrayList<Event> mEvents;


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
        args.putSerializable(ARG_EVENT, new ArrayList<Event>());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_EVENT)) {
                mEvents = (ArrayList<Event>) getArguments().getSerializable(ARG_EVENT);
            }
            selectedDate = (LocalDate) getArguments().getSerializable(ARG_DATE);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CALENDAR", "onResume: " + mEvents);
        loadEvents();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar_day, container, false);

        nextDay = rootView.findViewById(R.id.imageView_next_day);
        prevDay = rootView.findViewById(R.id.imageView_previous_day);
        textViewDay = rootView.findViewById(R.id.textView_month_day);
        textViewDay.setText(dayMonthFromDate(selectedDate));
        textViewNoEvents = rootView.findViewById(R.id.textView_no_events);

        recyclerView = rootView.findViewById(R.id.dayEventsRecyclerView);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        eventsAdapter = new EventsAdapter(mEvents, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(eventsAdapter);

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.plusDays(1);
                textViewDay.setText(dayMonthFromDate(selectedDate));
                textViewNoEvents.setVisibility(View.VISIBLE);
                eventsAdapter = new EventsAdapter(mEvents, getContext());
                recyclerView.setAdapter(eventsAdapter);
                loadEvents();
            }
        });

        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = selectedDate.minusDays(1);
                textViewDay.setText(dayMonthFromDate(selectedDate));
                textViewNoEvents.setVisibility(View.VISIBLE);
                eventsAdapter = new EventsAdapter(mEvents, getContext());
                recyclerView.setAdapter(eventsAdapter);
                loadEvents();
            }
        });

        return rootView;
    }

    private String dayMonthFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd");
        return date.format(formatter);
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

                                                    String eventDateTime = snap.get("eventTime").toString();
                                                    String[] eventDateDetails = eventDateTime.split(" ")[0].split("-");
                                                    int eventDay = Integer.parseInt(eventDateDetails[0]);
                                                    int eventMonth = Integer.parseInt(eventDateDetails[1]);
                                                    int eventYear = Integer.parseInt(eventDateDetails[2]);

                                                    if (eventYear == selectedDate.getYear()
                                                            && eventMonth == selectedDate.getMonth().getValue()
                                                            && eventDay == selectedDate.getDayOfMonth()) {
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
                                                        textViewNoEvents.setVisibility(View.GONE);
                                                        updateRecyclerView(events);
                                                    }
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
        eventsAdapter.setEvents(events);
        eventsAdapter.notifyDataSetChanged();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private void setEventsView() {

    }
}