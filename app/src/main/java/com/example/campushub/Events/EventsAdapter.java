package com.example.campushub.Events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

/**
 * Adapter for displying Events
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder>{

    private ArrayList<Event> events;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private IEventRowActions mListener;
    private Context mContext;

    public EventsAdapter() {
    }

    public EventsAdapter(ArrayList<Event> events, Context context) {
        this.events = events;
        if(context instanceof IEventRowActions){
            this.mContext = context;
            this.mListener = (IEventRowActions) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IEventRowActions");
        }
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView eventName;
        private final TextView eventOrganizer;
        private final TextView eventTime;
        private final TextView eventLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.eventName = itemView.findViewById(R.id.event_title);
            this.eventOrganizer = itemView.findViewById(R.id.event_organizer);
            this.eventTime = itemView.findViewById(R.id.event_date_time);
            this.eventLocation = itemView.findViewById(R.id.event_location);
        }

        public TextView getEventName() {
            return eventName;
        }

        public TextView getEventOrganizer() {
            return eventOrganizer;
        }

        public TextView getEventTime() {
            return eventTime;
        }

        public TextView getEventLocation() {
            return eventLocation;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.event_row_item,parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event currentEvent = this.getEvents().get(position);
        holder.getEventName().setText(currentEvent.getEventName());
        holder.getEventOrganizer().setText(currentEvent.getEventOwnerName());
        holder.getEventTime().setText(currentEvent.getEventTime());
        holder.getEventLocation().setText(currentEvent.getEventLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.loadEventDetailsFragment(currentEvent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.getEvents().size();
    }

    public interface IEventRowActions {
        void loadEventDetailsFragment(Event event);
    }
}
