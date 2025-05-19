package com.techevents.jobs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;


import com.techevents.model.Event;
import com.techevents.model.Subscriber;
import com.techevents.repository.EventRepository;
import com.techevents.repository.SubscriberRepository;
import com.techevents.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class DailyNotificationJobTest {

    @Mock private EventRepository eventRepository;
    @Mock private SubscriberRepository subscriberRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private DailyNotificationJob job;

    @Test
    void whenEventsExist_thenNotifyAllSubscribers() {
        Event e1 = new Event("Title", "desc", LocalDate.now(), "City", List.of("tag"));
        Subscriber s1 = new Subscriber("user1@example.com");
        Subscriber s2 = new Subscriber("user2@example.com");

        when(eventRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(e1));
        when(subscriberRepository.findAll()).thenReturn(List.of(s1, s2));

        job.sendDailySummaries();

        verify(notificationService).sendNotification(s1, List.of(e1));
        verify(notificationService).sendNotification(s2, List.of(e1));
    }

    @Test
    void whenNoEvents_thenSkipNotification() {
        when(eventRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        job.sendDailySummaries();

        verifyNoInteractions(subscriberRepository);
        verifyNoInteractions(notificationService);
    }
}
