package com.techevents.dto;

import java.util.List;

public class DailyEmail {
    private String email;
    private List<String> eventTitles;

    public DailyEmail() {}

    public DailyEmail(String email, List<String> eventTitles) {
        this.email = email;
        this.eventTitles = eventTitles;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getEventTitles() {
        return eventTitles;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEventTitles(List<String> eventTitles) {
        this.eventTitles = eventTitles;
    }
}
