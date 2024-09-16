package com.example.appwish.dto;

import java.time.LocalDateTime;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;

import lombok.Data;

@Data
public class MessageDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO recipient;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String mediaUrl;
    private Message.MessageType type;

    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSender(UserDTO.fromEntity(message.getSender()));
        if (message.getRecipient() != null) {
            dto.setRecipient(UserDTO.fromEntity(message.getRecipient()));
        }
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setReadAt(message.getReadAt());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setType(message.getType());
        return dto;
    }
}

@Data
class UserDTO {
    private Long id;
    private String username;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}