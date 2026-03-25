package com.notificationsAdrian.notificationsAdrian.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(String username, WebSocketSession session) {
        activeSessions.put(username, session);
        System.out.println("Usuario conectado: " + username);
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
        System.out.println("Usuario desconectado: " + username);
    }

    public List<String> getActiveSubscriberIds() {
        return new ArrayList<>(activeSessions.keySet());
    }

    public void broadcast(String message) {
        String jsonResponse = String.format("{\"type\":\"BROADCAST\", \"text\":\"%s\"}", message);
        sendMessageToAll(new TextMessage(jsonResponse));
    }

    public void broadcastUserList() {
        try {
            List<String> users = getActiveSubscriberIds();
            String usersJson = objectMapper.writeValueAsString(users);
            String messageStr = String.format("{\"type\":\"USER_LIST\", \"users\":%s}", usersJson);
            sendMessageToAll(new TextMessage(messageStr));
        } catch (Exception e) {
            System.err.println("Error enviando lista de usuarios");
        }
    }

    public void sendPrivateMessage(String senderName, String targetUser, String text, WebSocketSession senderSession) throws IOException {
        WebSocketSession targetSession = activeSessions.get(targetUser);

        if (targetSession != null && targetSession.isOpen()) {
            String jsonResponse = String.format("{\"type\":\"PRIVATE\", \"from\":\"%s\", \"text\":\"%s\"}", senderName, text);
            targetSession.sendMessage(new TextMessage(jsonResponse));
        } else {
            String errorMsg = String.format("{\"type\":\"SYSTEM\", \"text\":\"El usuario '%s' ya no está conectado.\"}", targetUser);
            if (senderSession.isOpen()) {
                senderSession.sendMessage(new TextMessage(errorMsg));
            }
        }
    }

    private void sendMessageToAll(TextMessage message) {
        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                }
            }
        }
    }
}