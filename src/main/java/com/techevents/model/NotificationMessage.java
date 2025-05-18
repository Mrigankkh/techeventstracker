package com.techevents.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationMessage {
    private String email;
    private String eventTitle;
    private String eventDate;
    private String city;
private String eventDescription;

    public NotificationMessage(String email, Event event) {
        this.email = email;
        this.eventTitle = event.getTitle();
        this.eventDate = event.getEventDate().toString(); 
        this.city = event.getCity();
        this.eventDescription = event.getDescription();

    }

    // Getters (and setters if needed)
    public String getEmail() {
        return email;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getCity() {
        return city;
    }
    public String getEventDescription() {
        return eventDescription;
    }
}
