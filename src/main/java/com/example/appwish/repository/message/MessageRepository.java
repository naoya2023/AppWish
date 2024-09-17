package com.example.appwish.repository.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.User;
import com.example.appwish.model.message.GroupChat;
import com.example.appwish.model.message.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, CustomMessageRepository {
    List<Message> findBySenderAndRecipientOrderBySentAtDesc(User sender, User recipient);
    
    List<Message> findByRecipientAndReadAtIsNullOrderBySentAtAsc(User recipient);
    
    List<Message> findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtDesc(User sender, User recipient, User recipient2, User sender2);
    
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    
    List<Message> findByRecipientOrderBySentAtDesc(User recipient);
    
    List<Message> findByGroupChatIdOrderBySentAtDesc(Long groupChatId);
    
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.recipient = :user ORDER BY m.sentAt DESC")
    List<Message> findMessagesForUser(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.groupChat = :groupChat AND m.sender != :user AND m.readAt IS NULL")
    long countUnreadMessagesForGroupChat(@Param("groupChat") GroupChat groupChat, @Param("user") User user);
    
    Message findTopByGroupChatOrderBySentAtDesc(GroupChat groupChat);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) AND m.readAt IS NULL")
    long countUnreadMessagesForConversation(@Param("user1") User user1, @Param("user2") User user2);
}