package com.notificationsAdrian.notificationsAdrian.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            activeSessions.put(username, session);
            System.out.println("Usuario conectado: " + username);

            broadcastUserList();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = getUsernameFromSession(session);
        if (username != null) {
            activeSessions.remove(username);
            System.out.println("Usuario desconectado: " + username);

            broadcastUserList();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String senderName = getUsernameFromSession(session);
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());

        String targetUser = jsonMessage.get("to").asText();
        String text = jsonMessage.get("text").asText();

        WebSocketSession targetSession = activeSessions.get(targetUser);

        if (targetSession != null && targetSession.isOpen()) {
            String jsonResponse = String.format("{\"type\":\"PRIVATE\", \"from\":\"%s\", \"text\":\"%s\"}", senderName, text);
            targetSession.sendMessage(new TextMessage(jsonResponse));
        } else {
            String errorMsg = String.format("{\"type\":\"SYSTEM\", \"text\":\"El usuario '%s' ya no está conectado.\"}", targetUser);
            session.sendMessage(new TextMessage(errorMsg));
        }
    }

    public void broadcast(String message) {
        String jsonResponse = String.format("{\"type\":\"BROADCAST\", \"text\":\"%s\"}", message);
        TextMessage textMessage = new TextMessage(jsonResponse);
        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try { session.sendMessage(textMessage); }
                catch (IOException e) {}
            }
        }
    }

    public List<String> getActiveSubscriberIds() {
        return new ArrayList<>(activeSessions.keySet());
    }

    private void broadcastUserList() {
        try {
            List<String> users = new ArrayList<>(activeSessions.keySet());
            String usersJson = objectMapper.writeValueAsString(users);
            String messageStr = String.format("{\"type\":\"USER_LIST\", \"users\":%s}", usersJson);

            TextMessage textMessage = new TextMessage(messageStr);
            for (WebSocketSession session : activeSessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enviando lista de usuarios");
        }
    }

    private String getUsernameFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("user=")) {
            return query.substring(5);
        }
        return session.getId();
    }
}
