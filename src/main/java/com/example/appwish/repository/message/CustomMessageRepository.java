package com.example.appwish.repository.message;

import java.util.List;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;

public interface CustomMessageRepository {
    List<Message> findLastMessagesForUser(User user);
    long countUnreadMessagesForConversation(User user, User otherUser);
    List<Message> searchMessages(User user, String query);
}