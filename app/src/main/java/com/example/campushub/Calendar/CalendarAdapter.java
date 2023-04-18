package com.example.campushub.Calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.Event;
import com.example.campushub.R;

import java.time.LocalDate;
import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final String TAG = "TAG";
    private final LocalDate today;
    private final ArrayList<String> daysOfMonth;
    private final OnCalendarItemListener onCalendarItemListener;
    private final boolean isCurrentMonth;
    private LocalDate selectedDate;
    private ArrayList<Event> events;

    public CalendarAdapter(LocalDate today,
                           ArrayList<String> daysOfMonth,
                           OnCalendarItemListener onCalendarItemListener,
                           boolean isCurrentMonth,
                           LocalDate selectedDate)
    {
        this.today = today;
        this.daysOfMonth = daysOfMonth;
        this.onCalendarItemListener = onCalendarItemListener;
        this.isCurrentMonth = isCurrentMonth;
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onCalendarItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        holder.getDayOfMonth().setText(daysOfMonth.get(position));

        if (!daysOfMonth.get(position).equals("")) {
            int todayPosition = today.getDayOfMonth();

            // Show todayCircle if day is today
            if (todayPosition == Integer.parseInt(daysOfMonth.get(position)) && isCurrentMonth) {
                holder.getTodayCircle().setVisibility(View.VISIBLE);
            } else {
                holder.getTodayCircle().setVisibility(View.GONE);
            }

            // Show eventDot if day has events
            if (dayHasEvents(Integer.parseInt(daysOfMonth.get(position)))) {
                holder.getEventDot().setVisibility(View.VISIBLE);
            } else {
                holder.getEventDot().setVisibility(View.GONE);
            }

        } else {
            holder.getTodayCircle().setVisibility(View.GONE);
            holder.getEventDot().setVisibility(View.GONE);
        }
    }

    private boolean dayHasEvents(int day) {
        if (events == null) {
            return false;
        }
        for (Event event : events) {
            String[] eventDateDetails = event.getEventTime().split(" ")[0].split("-");
            int eventDay = Integer.parseInt(eventDateDetails[0]);
            int eventMonth = Integer.parseInt(eventDateDetails[1]);
            int eventYear = Integer.parseInt(eventDateDetails[2]);

            if (eventYear == selectedDate.getYear()
                    && eventMonth == selectedDate.getMonth().getValue()
                    && eventDay == day) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public interface OnCalendarItemListener
    {
        void onItemClick(int position, String dayText);
    }
}