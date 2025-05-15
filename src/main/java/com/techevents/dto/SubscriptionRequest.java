package com.techevents.dto;

public class SubscriptionRequest {
    private String email;

    public SubscriptionRequest() {}

    public SubscriptionRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
