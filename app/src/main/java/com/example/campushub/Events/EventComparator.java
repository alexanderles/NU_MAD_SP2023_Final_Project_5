package com.example.campushub.Events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Class for comparing Events by date and time
 */
public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event event, Event t1) {
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        Date dateTime, dateTime1;
        try {
            dateTime = dateFormat.parse(event.getEventTime());
            dateTime1 = dateFormat.parse(t1.getEventTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return dateTime.compareTo(dateTime1);
    }
}
