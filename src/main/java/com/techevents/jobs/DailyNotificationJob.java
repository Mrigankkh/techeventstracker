package com.techevents.jobs;

import com.techevents.model.Event;
import com.techevents.model.Subscriber;
import com.techevents.repository.EventRepository;
import com.techevents.repository.SubscriberRepository;
import com.techevents.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DailyNotificationJob {

    private static final Logger log = LoggerFactory.getLogger(DailyNotificationJob.class);

    private final EventRepository eventRepository;
    private final SubscriberRepository subscriberRepository;
    private final NotificationService notificationService;

    public DailyNotificationJob(EventRepository eventRepository,
                                SubscriberRepository subscriberRepository,
                                NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.subscriberRepository = subscriberRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 20 * * *") 
    public void sendDailySummaries() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Event> events = eventRepository.findByCreatedAtBetween(start, end);
        if (events.isEmpty()) {
            log.info("No events created today — skipping daily notifications.");
            return;
        }

        List<Subscriber> subscribers = subscriberRepository.findAll();
        log.info("Sending daily summaries to {} subscribers", subscribers.size());

        for (Subscriber subscriber : subscribers) {
            try {
                notificationService.sendNotification(subscriber, events);
            } catch (Exception e) {
                log.error("Failed to send summary to {}", subscriber.getEmail(), e);
            }
        }

        log.info("Daily summary job complete");
    }
}
