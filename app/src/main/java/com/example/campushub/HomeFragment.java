package com.example.campushub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_EVENT = "events";

    private ArrayList<Event> mEvents;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private RecyclerView recyclerView;
    private EventsAdapter eventsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, new ArrayList<Event>());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_EVENT)) {
                mEvents = (ArrayList<Event>) args.getSerializable(ARG_EVENT);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = rootView.findViewById(R.id.upcoming_events_recyclerview);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        eventsAdapter = new EventsAdapter(mEvents, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(eventsAdapter);

        db.collection("users")
                .document(mUser.getEmail())
                .collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error == null){
//                            retrieving all the elements from Firebase....
                            ArrayList<Event> newEvents = new ArrayList<>();
                            for(DocumentSnapshot document : value.getDocuments()){
                                String eventReference = document.get("eventId").toString();
                                db.collection("events")
                                        .document(eventReference)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot snap = task.getResult();
                                                    Event newEvent = new Event(
                                                            eventReference,
                                                            snap.get("eventName").toString(),
                                                            snap.get("eventOwnerName").toString(),
                                                            snap.get("eventOwnerEmail").toString(),
                                                            snap.get("eventOrganizerImage").toString(),
                                                            snap.get("eventLocation").toString(),
                                                            snap.get("eventTime").toString(),
                                                            snap.get("eventDescription").toString()
                                                    );
                                                    DateTimeFormatter dateFormatter =
                                                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                                    LocalDate dateTime = LocalDate.parse(
                                                            newEvent.getEventTime(),
                                                            dateFormatter);
                                                    if (dateTime.compareTo(LocalDate.now()) > 0) {
                                                        newEvents.add(newEvent);
                                                    }
                                                }
                                            }
                                        });
                            }
                            eventsAdapter.setEvents(newEvents);
                            eventsAdapter.notifyDataSetChanged();
                        }
                    }
                });

        return rootView;
    }

    private void loadData() {
        ArrayList<Event> events = new ArrayList<>();

        db.collection("users")
                .document(mUser.getEmail())
                .collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot  document : task.getResult()){
                                String eventReference = document.get("eventId").toString();
                                db.collection("events")
                                        .document(eventReference)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot snap = task.getResult();
                                                    Event newEvent = new Event(
                                                            eventReference,
                                                            snap.get("eventName").toString(),
                                                            snap.get("eventOwnerName").toString(),
                                                            snap.get("eventOwnerEmail").toString(),
                                                            snap.get("eventOrganizerImage").toString(),
                                                            snap.get("eventLocation").toString(),
                                                            snap.get("eventTime").toString(),
                                                            snap.get("eventDescription").toString()
                                                    );
                                                    DateTimeFormatter dateFormatter =
                                                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                                    LocalDate dateTime = LocalDate.parse(
                                                            newEvent.getEventTime(),
                                                            dateFormatter);
                                                    if (dateTime.compareTo(LocalDate.now()) > 0) {
                                                        events.add(newEvent);
                                                    }
                                                }
                                            }
                                        });
                            }
                            updateRecyclerView(events);
                        }
                    }
                });
    }

    public void updateRecyclerView(ArrayList<Event> events){
        eventsAdapter.setEvents(events);
        eventsAdapter.notifyDataSetChanged();
    }
}