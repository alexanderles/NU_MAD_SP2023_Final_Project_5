package com.example.campushub.Organization;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.campushub.Events.Event;
import com.example.campushub.Events.EventComparator;
import com.example.campushub.Events.EventsAdapter;
import com.example.campushub.R;
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

/**
 * Fragment representing an Organization as viewed by the owner of the organization
 */
public class OrgProfileOwnerView extends Fragment {
    private static final String ARG_EVENT = "events";

    private TextView orgName, orgEmail;
    private ImageView orgImage;

    private Button addEvent, logout;

    private ArrayList<Event> mEvents;
    private IOrgProfileOwnerActions mListener;
    private String orgProfileImage;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;

    private RecyclerView recyclerView;
    private EventsAdapter eventsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    public OrgProfileOwnerView() {
        // Required empty public constructor
    }


    public static OrgProfileOwnerView newInstance() {
        OrgProfileOwnerView fragment = new OrgProfileOwnerView();
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
        storage = FirebaseStorage.getInstance();

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_org_profile_owner_view, container, false);

        orgName = rootView.findViewById(R.id.org_name_owner_view);
        orgEmail = rootView.findViewById(R.id.org_email_owner_view);
        orgImage = rootView.findViewById(R.id.user_profile_image);

        addEvent = rootView.findViewById(R.id.button_edit_profile);
        logout = rootView.findViewById(R.id.button_sign_out_user);

        recyclerView = rootView.findViewById(R.id.organization_events_recyclerview);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        eventsAdapter = new EventsAdapter(mEvents, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(eventsAdapter);

        db.collection("Org_Users")
                .document(mUser.getEmail())
                .collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error == null){
                            // retrieving all the elements from Firebase....
                            ArrayList<Event> newEvents = new ArrayList<>();
                            for(DocumentSnapshot document : value.getDocuments()){
                                String eventReference = document.get("eventId").toString();
                                db.collection("events")
                                        .document(eventReference)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot snap = task.getResult();
                                                if (task.isSuccessful()) {
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
                                                    DateTimeFormatter dateFormatter =
                                                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                                    LocalDate dateTime = LocalDate.parse(
                                                            newEvent.getEventTime(),
                                                            dateFormatter);
                                                    if (dateTime.compareTo(LocalDate.now()) >= 0) {
                                                        newEvents.add(newEvent);
                                                    }
                                                    newEvents.sort(new EventComparator());
                                                    eventsAdapter.setEvents(newEvents);
                                                    eventsAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addEvent();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.orgProfileLogout();
            }
        });

        return rootView;
    }

    private void loadData() {
        // LOAD USER INFORMATION
        db.collection("Org_Users")
                .document(mUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snap = task.getResult();
                            orgName.setText(snap.get("Org_Name").toString());
                            orgEmail.setText(snap.get("email").toString());
                            Object imageStorage = snap.get("profileImage");
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
                                                    DateTimeFormatter dateFormatter =
                                                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                                    LocalDate dateTime = LocalDate.parse(
                                                            newEvent.getEventTime(),
                                                            dateFormatter);
                                                    if (dateTime.compareTo(LocalDate.now()) >= 0) {
                                                        events.add(newEvent);
                                                    }
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
        eventsAdapter.setEvents(events);
        eventsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOrgProfileOwnerActions){
            this.mListener = (IOrgProfileOwnerActions) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IOrgProfileOwnerActions");
        }
    }

    public interface IOrgProfileOwnerActions {
        void orgProfileLogout();
        void addEvent();
    }
}