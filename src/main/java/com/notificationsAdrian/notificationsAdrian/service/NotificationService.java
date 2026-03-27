package com.notificationsAdrian.notificationsAdrian.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationsAdrian.notificationsAdrian.model.ChatMessage;
import com.notificationsAdrian.notificationsAdrian.repository.ChatMessageRepository;
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

    private final ChatMessageRepository messageRepository;

    public NotificationService(ChatMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void addSession(String username, WebSocketSession session) {
        activeSessions.put(username, session);
        System.out.println("Usuario conectado: " + username);

        sendHistoryToUser(username, session);
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
        System.out.println("Usuario desconectado: " + username);
    }

    public List<String> getActiveSubscriberIds() {
        return new ArrayList<>(activeSessions.keySet());
    }

    public void broadcast(String message) {
        ChatMessage dbMessage = new ChatMessage("SISTEMA", "GLOBAL", message, "BROADCAST");
        messageRepository.save(dbMessage);

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
        ChatMessage dbMessage = new ChatMessage(senderName, targetUser, text, "PRIVATE");
        messageRepository.save(dbMessage);

        WebSocketSession targetSession = activeSessions.get(targetUser);

        if (targetSession != null && targetSession.isOpen()) {
            String jsonResponse = String.format("{\"type\":\"PRIVATE\", \"from\":\"%s\", \"text\":\"%s\"}", senderName, text);
            targetSession.sendMessage(new TextMessage(jsonResponse));
        } else {
            String errorMsg = String.format("{\"type\":\"SYSTEM\", \"text\":\"El usuario '%s' ya no está conectado. Mensaje guardado para cuando vuelva.\"}", targetUser);
            if (senderSession.isOpen()) {
                senderSession.sendMessage(new TextMessage(errorMsg));
            }
        }
    }

    private void sendMessageToAll(TextMessage message) {
        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try { session.sendMessage(message); } catch (IOException e) {}
            }
        }
    }

    private void sendHistoryToUser(String username, WebSocketSession session) {
        List<ChatMessage> history = messageRepository.findByReceiverOrSenderOrReceiverOrderByTimestampAsc(username, username, "GLOBAL");

        try {
            for (ChatMessage msg : history) {
                String jsonResponse = "";
                if (msg.getMessageType().equals("BROADCAST")) {
                    jsonResponse = String.format("{\"type\":\"BROADCAST\", \"text\":\"%s\"}", msg.getContent());
                } else if (msg.getMessageType().equals("PRIVATE")) {
                    if (msg.getSender().equals(username)) {
                        jsonResponse = String.format("{\"type\":\"HISTORY_SENT\", \"to\":\"%s\", \"text\":\"%s\"}", msg.getReceiver(), msg.getContent());
                    } else {
                        jsonResponse = String.format("{\"type\":\"PRIVATE\", \"from\":\"%s\", \"text\":\"%s\"}", msg.getSender(), msg.getContent());
                    }
                }

                if (!jsonResponse.isEmpty() && session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonResponse));
                }
            }
        } catch (Exception e) {
            System.err.println("Error enviando historial: " + e.getMessage());
        }
    }
}