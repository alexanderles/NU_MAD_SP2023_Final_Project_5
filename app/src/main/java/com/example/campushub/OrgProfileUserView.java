package com.example.campushub;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class OrgProfileUserView extends Fragment {

    private static final String ARG_ORG = "organization";
    private static final String ARG_EVENT = "events";

    private ArrayList<Event> mEvents;
    private String orgProfileImage;
    private String organizerEmail;

    private TextView orgName, orgEmail;
    private ImageView orgImage;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;

    private RecyclerView recyclerView;
    private EventsAdapter eventsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    public OrgProfileUserView() {
        // Required empty public constructor
    }

    public static OrgProfileUserView newInstance(String organizerEmail) {
        OrgProfileUserView fragment = new OrgProfileUserView();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, new ArrayList<Event>());
        args.putString(ARG_ORG, organizerEmail);
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
            organizerEmail = args.getString(ARG_ORG);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();

        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_org_profile_user_view, container, false);

        orgName = rootView.findViewById(R.id.org_name_user_view);
        orgEmail = rootView.findViewById(R.id.org_email_user_view);
        orgImage = rootView.findViewById(R.id.user_profile_image);

        recyclerView = rootView.findViewById(R.id.organization_events_recyclerview);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        eventsAdapter = new EventsAdapter(mEvents, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(eventsAdapter);

        db.collection("Org_Users")
                .document(organizerEmail)
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
        // LOAD USER INFORMATION
        db.collection("Org_Users")
                .document(organizerEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snap = task.getResult();
                            orgName.setText(snap.get("Org_Name").toString());
                            orgEmail.setText(snap.get("email").toString());
                            Object imageStorage = snap.get("Org_Image");
                            if (imageStorage != null) {
                                orgProfileImage = imageStorage.toString();
                                StorageReference imageToLoad = storage.getReference().child(orgProfileImage);
                                imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful() && isAdded()) {
                                            Glide.with(getActivity())
                                                    .load(task.getResult())
                                                    .centerCrop()
                                                    .into(orgImage);
                                        }
                                        else if (isAdded()) {
                                            Toast.makeText(getActivity(),
                                                    "Unable to download image.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

        // LOAD EVENTS
        ArrayList<Event> events = new ArrayList<>();

        db.collection("Org_Users")
                .document(organizerEmail)
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