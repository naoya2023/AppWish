package com.example.appwish.repository.message;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class CustomMessageRepositoryImpl implements CustomMessageRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Message> findLastMessagesForUser(User user) {
        String jpql = "SELECT m FROM Message m WHERE (m.sender = :user OR m.recipient = :user) AND m.id IN " +
                      "(SELECT MAX(m2.id) FROM Message m2 WHERE m2.sender = :user OR m2.recipient = :user " +
                      "GROUP BY CASE WHEN m2.sender = :user THEN m2.recipient ELSE m2.sender END) " +
                      "ORDER BY m.sentAt DESC";
        TypedQuery<Message> query = entityManager.createQuery(jpql, Message.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    public long countUnreadMessagesForConversation(User user, User otherUser) {
        String jpql = "SELECT COUNT(m) FROM Message m WHERE m.recipient = :user AND m.sender = :otherUser AND m.readAt IS NULL";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("user", user);
        query.setParameter("otherUser", otherUser);
        return query.getSingleResult();
    }

    @Override
    public List<Message> searchMessages(User user, String query) {
        String jpql = "SELECT m FROM Message m WHERE (m.sender = :user OR m.recipient = :user) AND " +
                      "(m.content LIKE :query OR m.sender.username LIKE :query OR m.recipient.username LIKE :query)";
        TypedQuery<Message> typedQuery = entityManager.createQuery(jpql, Message.class);
        typedQuery.setParameter("user", user);
        typedQuery.setParameter("query", "%" + query + "%");
        return typedQuery.getResultList();
    }
}