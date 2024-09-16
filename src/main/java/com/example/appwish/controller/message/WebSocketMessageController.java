package com.example.appwish.controller.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.appwish.dto.MessageDTO;
import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;
import com.example.appwish.service.UserService;
import com.example.appwish.service.message.MessageService;

@Controller
public class WebSocketMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null && auth.isAuthenticated()) {
            User sender = userService.getCurrentUser(auth);
            if (sender != null) {
                message.setSender(sender);
                message = messageService.saveMessage(message);
                
                MessageDTO messageDTO = MessageDTO.fromEntity(message);
                
                messagingTemplate.convertAndSendToUser(
                    message.getRecipient().getUsername(),
                    "/queue/private",
                    messageDTO
                );
                messagingTemplate.convertAndSendToUser(
                    sender.getUsername(),
                    "/queue/private",
                    messageDTO
                );
            } else {
                throw new IllegalStateException("Authenticated user not found in the database");
            }
        } else {
            throw new IllegalStateException("User not authenticated");
        }
    }

    @MessageMapping("/chat.sendGroup")
    public void sendGroupMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null && auth.isAuthenticated()) {
            User sender = userService.getCurrentUser(auth);
            if (sender != null) {
                message.setSender(sender);
                message = messageService.saveMessage(message);
                
                MessageDTO messageDTO = MessageDTO.fromEntity(message);
                
                messagingTemplate.convertAndSend(
                    "/topic/group/" + message.getGroupChat().getId(),
                    messageDTO
                );
            } else {
                throw new IllegalStateException("Authenticated user not found in the database");
            }
        } else {
            throw new IllegalStateException("User not authenticated");
        }
    }
}