package com.notificationsAdrian.notificationsAdrian.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;
    private String content;
    private String messageType;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, String receiver, String content, String messageType) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
