package com.notificationsAdrian.notificationsAdrian.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationsAdrian.notificationsAdrian.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationWebSocketHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            notificationService.addSession(username, session);
            notificationService.broadcastUserList();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            notificationService.removeSession(username);
            notificationService.broadcastUserList();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String senderName = getUsernameFromSession(session);
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());

        String targetUser = jsonMessage.get("to").asText();
        String text = jsonMessage.get("text").asText();

        notificationService.sendPrivateMessage(senderName, targetUser, text, session);
    }

    private String getUsernameFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("user=")) {
            return query.substring(5);
        }
        return session.getId();
    }
}
