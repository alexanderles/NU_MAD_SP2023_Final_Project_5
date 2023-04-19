package com.example.campushub.Events;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.campushub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserEventView extends Fragment {

    private static final String ARG_EVENT = "event";

    private String eventRegisterRef = null;

    private Event eventDetails;
    private OwnerEventView.IOwnerEventDetailsActions mListener;

    private boolean registered;

    private TextView eventName, organizerName, organizerEmail,
            eventTime, eventLocation, eventDescription;
    private String orgImagePath;
    private ImageView organizerImage;
    private Button register;


    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;

    public UserEventView() {
        // Required empty public constructor
    }

    public static UserEventView newInstance(Event event) {
        UserEventView fragment = new UserEventView();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_EVENT)) {
                eventDetails = (Event) args.getSerializable(ARG_EVENT);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_event_view, container, false);

        eventName = rootView.findViewById(R.id.event_details_title);
        organizerName = rootView.findViewById(R.id.event_details_organizer);
        organizerEmail = rootView.findViewById(R.id.event_details_organizer_email);
        eventTime = rootView.findViewById(R.id.event_details_date);
        eventLocation = rootView.findViewById(R.id.event_details_location);
        eventDescription = rootView.findViewById(R.id.event_details_description);
        organizerImage = rootView.findViewById(R.id.event_details_image);

        register = rootView.findViewById(R.id.button_user_event_register);

        eventName.setText(eventDetails.getEventName());
        organizerName.setText(eventDetails.getEventOwnerName());
        organizerEmail.setText(eventDetails.getEventOwnerEmail());
        eventTime.setText(eventDetails.getEventTime());
        eventLocation.setText(eventDetails.getEventLocation());
        eventDescription.setText(eventDetails.getEventDescription());

        if (eventDetails.getEventOrganizerImage() != null) {
            orgImagePath = eventDetails.getEventOrganizerImage();
            StorageReference imageToLoad = storage.getReference().child(orgImagePath);
            imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful() && isAdded()) {
                        Glide.with(getActivity())
                                .load(task.getResult())
                                .centerCrop()
                                .into(organizerImage);
                    }
                    else if (isAdded()) {
                        Toast.makeText(getActivity(),
                                "Unable to download image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        loadRegistration();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });


        return rootView;
    }

    private void loadRegistration() {
        db.collection("Member_Users")
                .document(mUser.getEmail())
                .collection("events")
                .whereEqualTo("eventId", eventDetails.getEventId())
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot results = task.getResult();
                            registered = !results.isEmpty();
                            if (registered) {
                                for (QueryDocumentSnapshot doc : results) {
                                    eventRegisterRef = doc.getId();
                                }
                            }
                            updateRegisterButton();
                        }
                        else {
                            registered = false;
                        }
                    }
                });
    }

    private void updateRegisterButton() {
        if (registered) {
            register.setBackgroundColor(getResources().getColor(R.color.campus_hub_red));
            register.setText("Deregister");
        }
        else {
            register.setBackgroundColor(getResources().getColor(R.color.campus_hub_color));
            register.setText("Register");
        }
    }

    private void handleRegister() {
        if (registered && eventRegisterRef != null) {
            db.collection("Member_Users")
                    .document(mUser.getEmail())
                    .collection("events")
                    .document(eventRegisterRef)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                registered = false;
                                updateRegisterButton();
                                Toast.makeText(getActivity(), "Deregistered from event", Toast.LENGTH_LONG);
                            } else {
                                Toast.makeText(getActivity(), "Failed to deregister", Toast.LENGTH_LONG);
                            }
                        }
                    });
        }
        else if (!registered) {
            Map<String, String> newEventRef = new HashMap<>();
            newEventRef.put("eventId", eventDetails.getEventId());
            db.collection("Member_Users")
                    .document(mUser.getEmail())
                    .collection("events")
                    .add(newEventRef)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Registered for event", Toast.LENGTH_LONG);
                                eventRegisterRef = task.getResult().getId();
                                registered = true;
                                updateRegisterButton();
                            }
                            else {
                                Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_LONG);
                            }
                        }
                    });
        }
        else {
            Toast.makeText(getActivity(), "Invalid event reference retrieved", Toast.LENGTH_LONG);
        }
    }

}