package com.notificationsAdrian.notificationsAdrian.controller;

import com.notificationsAdrian.notificationsAdrian.dto.NotificationRequest;
import com.notificationsAdrian.notificationsAdrian.service.NotificationService;
import com.notificationsAdrian.notificationsAdrian.websocket.NotificationWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publisher")
public class PublisherController {
    private final NotificationService notificationService;

    public PublisherController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.broadcast(request.getMessage());
        return ResponseEntity.ok("Notification successfully sent to active subscribers");
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<String>> getSubscribers() {
        return ResponseEntity.ok(notificationService.getActiveSubscriberIds());
    }
}
