package com.example.appwish.repository.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, CustomMessageRepository {
    List<Message> findBySenderAndRecipientOrderBySentAtDesc(User sender, User recipient);
    
    List<Message> findByRecipientAndReadAtIsNullOrderBySentAtAsc(User recipient);
    
    List<Message> findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtDesc(User sender, User recipient, User recipient2, User sender2);
    
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    
    List<Message> findByRecipientOrderBySentAtDesc(User recipient);
    
    List<Message> findByGroupChatIdOrderBySentAtDesc(Long groupChatId);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.recipient = :user) " +
           "AND m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE m2.sender = :user OR m2.recipient = :user " +
           "GROUP BY CASE WHEN m2.sender = :user THEN m2.recipient.id ELSE m2.sender.id END) " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLastMessagesForUser(@Param("user") User user);
    
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.recipient = :user ORDER BY m.sentAt DESC")
    List<Message> findMessagesForUser(@Param("user") User user);
    
}