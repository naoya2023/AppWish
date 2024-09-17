package com.example.appwish.service.message;

import com.example.appwish.model.User;
import com.example.appwish.model.message.GroupChat;
import com.example.appwish.model.message.Message;

public class ConversationPreview {
    private Long id;
    private String name;
    private User otherUser;
    private Message lastMessage;
    private boolean hasUnread;
    private boolean isGroupChat;

    // 個人チャット用のコンストラクタ
    public ConversationPreview(User otherUser, Message lastMessage, boolean hasUnread) {
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.hasUnread = hasUnread;
        this.isGroupChat = false;
    }

    // グループチャット用のコンストラクタ
    public ConversationPreview(GroupChat groupChat, Message lastMessage, boolean hasUnread) {
        this.id = groupChat.getId();
        this.name = groupChat.getName();
        this.lastMessage = lastMessage;
        this.hasUnread = hasUnread;
        this.isGroupChat = true;
    }

    // ゲッターとセッター
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isHasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }
    
    
}