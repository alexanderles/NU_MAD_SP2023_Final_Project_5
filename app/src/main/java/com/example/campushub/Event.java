package com.example.campushub;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventId;
    private String eventName;
    private String eventOwnerName;
    private String eventOwnerEmail;
    private String eventOrganizerImage;
    private String eventLocation;
    private String eventTime;
    private String eventDescription;

    public Event() {
    }

    public Event(String eventId,
                 String eventName,
                 String eventOwnerName,
                 String eventOwnerEmail,
                 String eventOrganizerImage,
                 String eventLocation,
                 String eventTime,
                 String eventDescription) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventOwnerName = eventOwnerName;
        this.eventOwnerEmail = eventOwnerEmail;
        this.eventOrganizerImage = eventOrganizerImage;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
        this.eventDescription = eventDescription;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventOwnerName() {
        return eventOwnerName;
    }

    public void setEventOwnerName(String eventOwnerName) {
        this.eventOwnerName = eventOwnerName;
    }

    public String getEventOwnerEmail() {
        return eventOwnerEmail;
    }

    public void setEventOwnerEmail(String eventOwnerEmail) {
        this.eventOwnerEmail = eventOwnerEmail;
    }

    public String getEventOrganizerImage() {
        return eventOrganizerImage;
    }

    public void setEventOrganizerImage(String eventOrganizerImage) {
        this.eventOrganizerImage = eventOrganizerImage;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventOwnerName='" + eventOwnerName + '\'' +
                ", eventOwnerEmail='" + eventOwnerEmail + '\'' +
                ", eventOrganizerImage='" + eventOrganizerImage + '\'' +
                ", eventLocation='" + eventLocation + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                '}';
    }
}
