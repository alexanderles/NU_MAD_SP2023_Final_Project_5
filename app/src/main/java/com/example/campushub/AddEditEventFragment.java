package com.example.campushub;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddEditEventFragment extends Fragment {

    private static final String ARG_EVENT = "event";

    private Event eventToEdit = null;

    private String orgName, orgEmail;
    private String orgImage = null;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;

    private IAddEditEventActions mListener;

    private EditText eventName, eventLocation, eventDescription, eventTime;
    private CalendarView eventDate;
    private Button sendEventUpdates;

    public AddEditEventFragment() {
        // Required empty public constructor
    }

    public static AddEditEventFragment newInstance(Event event) {
        AddEditEventFragment fragment = new AddEditEventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventToEdit = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        loadUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_edit_event, container, false);

        eventName = rootView.findViewById(R.id.edit_event_name);
        eventLocation = rootView.findViewById(R.id.edit_event_location);
        eventTime = rootView.findViewById(R.id.edit_event_time);
        eventDescription = rootView.findViewById(R.id.edit_event_description);
        eventDate = rootView.findViewById(R.id.edit_event_date);
        sendEventUpdates = rootView.findViewById(R.id.send_event_changes_button);

        if (eventToEdit != null) {
            loadEventInfo();
            sendEventUpdates.setText("Update");
        }

        sendEventUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (eventName.getText().toString().equals("") ||
                    eventLocation.getText().toString().equals("") ||
                    eventTime.getText().toString().equals("") ||
                    eventDescription.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Fill in all fields", Toast.LENGTH_LONG);
                    return;
                }
                if (eventToEdit != null) {
                    updateEvent();
                }
                else {
                    createEvent();
                }
            }
        });


        return rootView;
    }

    private void loadUser() {
        db.collection("Org_Users")
                .document(mUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snap = task.getResult();
                            orgName = snap.get("Org_Name").toString();
                            orgEmail = snap.get("email").toString();
                            Object imagePathReturn = snap.get("Org_Image");
                            if (imagePathReturn != null) {
                                orgImage = imagePathReturn.toString();
                            }
                        }
                    }
                });
    }

    private void loadEventInfo() {
        eventName.setText(eventToEdit.getEventName());
        eventLocation.setText(eventToEdit.getEventLocation());
        String eventTimeString = eventToEdit.getEventTime().substring(11, 15);
        eventTime.setText(eventTimeString);
        eventDescription.setText(eventToEdit.getEventDescription());
        String eventDateString = eventToEdit.getEventTime().substring(0, 9);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
        Date dateOfEvent = null;
        try {
            dateOfEvent = sdf.parse(eventDateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        eventDate.setDate(dateOfEvent.getTime());
    }

    private void updateEvent() {
        final DocumentReference eventInfo = db.collection("events")
                .document(eventToEdit.getEventId());

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(eventInfo, "eventName", eventName.getText().toString());
                transaction.update(eventInfo, "eventLocation", eventLocation.getText().toString());
                transaction.update(eventInfo, "eventDescription", eventDescription.getText().toString());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
                String eventDateFormat = sdf.format(new Date(eventDate.getDate()));
                String eventDateTime = eventDateFormat + ' ' + eventTime.getText().toString();
                transaction.update(eventInfo, "eventTime", eventDateTime);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mListener.loadOrgProfile();
                }
                else {
                    Toast.makeText(getActivity(),
                            "Unable to update profile.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void createEvent() {
        Map<String, String> newEvent = new HashMap<>();
        newEvent.put("eventName", eventName.getText().toString());
        newEvent.put("eventLocation", eventLocation.getText().toString());
        newEvent.put("eventDescription", eventDescription.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
        String eventDateFormat = sdf.format(new Date(eventDate.getDate()));
        String eventDateTime = eventDateFormat + ' ' + eventTime.getText().toString();
        newEvent.put("eventTime", eventDateTime);
        newEvent.put("eventOwnerName", orgName);
        newEvent.put("eventOwnerEmail", orgEmail);
        if (orgImage != null) {
            newEvent.put("eventOrganizerImage", orgImage);
        } else {
            newEvent.put("eventOrganizerImage", null);
        }

        db.collection("events")
                .add(newEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference newEventDoc = task.getResult();
                            String newEventId = newEventDoc.getId();
                            Map<String, String> newEventReference = new HashMap<>();
                            newEventReference.put("eventId", newEventId);
                            db.collection("Org_Users")
                                    .document(mUser.getEmail())
                                    .collection("events")
                                    .add(newEventReference)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                mListener.loadOrgProfile();
                                            }
                                            else {
                                                Toast.makeText(getActivity(),
                                                        "Unable to add event.", Toast.LENGTH_LONG);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IAddEditEventActions){
            this.mListener = (IAddEditEventActions) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IAddEditEventActions");
        }
    }

    public interface IAddEditEventActions {
        void loadOrgProfile();
    }
}