package com.techevents.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDate eventDate;

    private String city;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    private List<String> tags;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public Event() {
    }

    public Event(String title, String description, LocalDate eventDate, String city,
            List<String> tags) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.city = city;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getCity() {
        return city;
    }

    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
