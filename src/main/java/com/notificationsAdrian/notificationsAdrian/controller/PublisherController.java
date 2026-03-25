package com.notificationsAdrian.notificationsAdrian.controller;

import com.notificationsAdrian.notificationsAdrian.dto.NotificationRequest;
import com.notificationsAdrian.notificationsAdrian.websocket.NotificationWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publisher")
public class PublisherController {
    private final NotificationWebSocketHandler webSocketHandler;

    public PublisherController(NotificationWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        webSocketHandler.broadcast(request.getMessage());
        return ResponseEntity.ok("Notificación enviada con éxito a los suscriptores activos.");
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<String>> getSubscribers() {
        return ResponseEntity.ok(webSocketHandler.getActiveSubscriberIds());
    }
}
