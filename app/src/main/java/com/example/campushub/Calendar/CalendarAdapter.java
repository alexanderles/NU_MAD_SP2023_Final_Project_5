package com.example.campushub.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.R;

import java.time.LocalDate;
import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final LocalDate today;
    private final ArrayList<String> daysOfMonth;
    private final OnCalendarItemListener onCalendarItemListener;
    private final boolean isCurrentMonth;

    public CalendarAdapter(LocalDate today,
                           ArrayList<String> daysOfMonth,
                           OnCalendarItemListener onCalendarItemListener,
                           boolean isCurrentMonth)
    {
        this.today = today;
        this.daysOfMonth = daysOfMonth;
        this.onCalendarItemListener = onCalendarItemListener;
        this.isCurrentMonth = isCurrentMonth;
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
        holder.dayOfMonth.setText(daysOfMonth.get(position));

        if (!daysOfMonth.get(position).equals("")) {
            int todayPosition = today.getDayOfMonth();
            if (todayPosition == Integer.parseInt(daysOfMonth.get(position)) && isCurrentMonth) {
                holder.todayCircle.setVisibility(View.VISIBLE);
            } else {
                holder.todayCircle.setVisibility(View.GONE);
            }
        } else {
            holder.todayCircle.setVisibility(View.GONE);
            holder.eventDot.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface OnCalendarItemListener
    {
        void onItemClick(int position, String dayText);
    }
}