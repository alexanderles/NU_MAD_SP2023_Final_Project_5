package com.example.campushub.Calendar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView dayOfMonth;
    public final ImageView todayCircle;
    public final ImageView eventDot;
    private final CalendarAdapter.OnCalendarItemListener onCalendarItemListener;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnCalendarItemListener onCalendarItemListener)
    {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        todayCircle = itemView.findViewById(R.id.imageView_circle_today);
        eventDot = itemView.findViewById(R.id.imageView_event_dot);
        this.onCalendarItemListener = onCalendarItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onCalendarItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
