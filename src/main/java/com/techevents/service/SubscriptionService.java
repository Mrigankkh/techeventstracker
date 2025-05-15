package com.techevents.service;

import com.techevents.dto.SubscriptionRequest;
import com.techevents.model.Subscriber;
import com.techevents.repository.SubscriberRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriberRepository subscriberRepository;

    public SubscriptionService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public void subscribe(SubscriptionRequest request) {
        if (subscriberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already subscribed");
        }

        Subscriber subscriber = new Subscriber(request.getEmail());
        subscriberRepository.save(subscriber);
    }
}
