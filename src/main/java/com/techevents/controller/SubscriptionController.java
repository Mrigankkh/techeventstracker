package com.techevents.controller;

import com.techevents.dto.SubscriptionRequest;
import com.techevents.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscribe")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest request) {
        try {
            subscriptionService.subscribe(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Subscribed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
