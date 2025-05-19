package com.techevents.repository;

import com.techevents.model.Event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
