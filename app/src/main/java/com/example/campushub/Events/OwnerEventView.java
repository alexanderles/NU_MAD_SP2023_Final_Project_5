package com.example.campushub.Events;

import android.content.Context;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.campushub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class OwnerEventView extends Fragment {

    private static final String ARG_EVENT = "event";

    private Event eventDetails;
    private IOwnerEventDetailsActions mListener;

    private TextView eventName, organizerName, organizerEmail,
        eventTime, eventLocation, eventDescription;
    private String orgImagePath;
    private ImageView organizerImage;
    private Button editEvent, deleteEvent;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;

    public OwnerEventView() {
        // Required empty public constructor
    }

    public static OwnerEventView newInstance(Event event) {
        OwnerEventView fragment = new OwnerEventView();
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
        View rootView = inflater.inflate(R.layout.fragment_owner_event_view, container, false);

        eventName = rootView.findViewById(R.id.event_details_title_owner);
        organizerName = rootView.findViewById(R.id.event_details_organizer_owner);
        organizerEmail = rootView.findViewById(R.id.event_details_organizer_email_owner);
        eventTime = rootView.findViewById(R.id.event_details_date_owner);
        eventLocation = rootView.findViewById(R.id.event_details_location_owner);
        eventDescription = rootView.findViewById(R.id.event_details_description_owner);
        organizerImage = rootView.findViewById(R.id.event_details_image_owner);

        editEvent = rootView.findViewById(R.id.button_user_event_edit_owner);
        deleteEvent = rootView.findViewById(R.id.button_user_event_delete_owner);

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

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.editEvent(eventDetails);
            }
        });

        deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("events")
                        .document(eventDetails.getEventId())
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    db.collection("Org_Users")
                                            .document(mUser.getEmail())
                                            .collection("events")
                                            .whereEqualTo("eventId", eventDetails.getEventId())
                                            .limit(1)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                                            String eventRef = doc.getId();
                                                            db.collection("Org_Users")
                                                                    .document(mUser.getEmail())
                                                                    .collection("events")
                                                                    .document(eventRef)
                                                                    .delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            mListener.deleteEventRedirect();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(getActivity(),
                                                                                    "Unable to download image.", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });
                                }
                                else {
                                    Toast.makeText(getActivity(),
                                            "Unable to delete event.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        db.collection("events")
                .document(eventDetails.getEventId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            eventDetails.setEventName(value.get("eventName").toString());
                            eventDetails.setEventLocation(value.get("eventLocation").toString());
                            eventDetails.setEventTime(value.get("eventTime").toString());
                            eventDetails.setEventDescription(value.get("eventDescription").toString());

                            eventName.setText(eventDetails.getEventName());
                            organizerName.setText(eventDetails.getEventOwnerName());
                            organizerEmail.setText(eventDetails.getEventOwnerEmail());
                            eventTime.setText(eventDetails.getEventTime());
                            eventLocation.setText(eventDetails.getEventLocation());
                            eventDescription.setText(eventDetails.getEventDescription());
                        }
                    }
                });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOwnerEventDetailsActions){
            this.mListener = (IOwnerEventDetailsActions) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IOwnerEventDetailsActions");
        }
    }

    public interface IOwnerEventDetailsActions {
        void editEvent(Event event);
        void deleteEventRedirect();
    }
}