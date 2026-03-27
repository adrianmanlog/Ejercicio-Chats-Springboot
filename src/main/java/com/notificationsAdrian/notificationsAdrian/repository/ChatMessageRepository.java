package com.notificationsAdrian.notificationsAdrian.repository;

import com.notificationsAdrian.notificationsAdrian.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByReceiverOrSenderOrReceiverOrderByTimestampAsc(String receiver1, String sender, String receiver2);
}