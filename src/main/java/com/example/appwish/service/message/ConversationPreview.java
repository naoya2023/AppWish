package com.example.appwish.service.message;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;

import lombok.Data;

@Data
public class ConversationPreview {
    private User otherUser;
    private Message lastMessage;
    private boolean hasUnread;

    public ConversationPreview(User otherUser, Message lastMessage, boolean hasUnread) {
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.hasUnread = hasUnread;
    }
}