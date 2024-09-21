package com.example.appwish.service.message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appwish.model.User;
import com.example.appwish.model.message.GroupChat;
import com.example.appwish.model.message.Message;
import com.example.appwish.model.project.Project;
import com.example.appwish.repository.UserRepository;
import com.example.appwish.repository.message.GroupChatRepository;
import com.example.appwish.repository.message.MessageRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Transactional
    public Message sendMessage(User sender, User recipient, String content) {
        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("送信者または受信者が無効です。");
        }
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setType(Message.MessageType.CHAT);
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getConversation(User user1, User user2) {
        return messageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtDesc(user1, user2, user1, user2);
    }

    public List<Message> getUnreadMessages(User recipient) {
        return messageRepository.findByRecipientAndReadAtIsNullOrderBySentAtAsc(recipient);
    }

    @Transactional
    public void markAsRead(Message message) {
        if (message.getReadAt() == null) {
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    @Transactional
    public Message sendGroupMessage(User sender, Long groupChatId, String content) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
        Message message = new Message();
        message.setSender(sender);
        message.setGroupChat(groupChat);
        message.setContent(content);
        message.setType(Message.MessageType.CHAT);
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getGroupConversation(Long groupChatId) {
        return messageRepository.findByGroupChatIdOrderBySentAtDesc(groupChatId);
    }

    public List<Message> getRecentMessages(User user) {
        List<Message> lastMessages = messageRepository.findMessagesForUser(user);
        return lastMessages.stream()
                .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Transactional
    public Message saveMessage(Message message) {
        if (message.getSender() == null) {
            throw new IllegalArgumentException("送信者が無効です。");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("メッセージの内容が無効です。");
        }
        message.setSentAt(LocalDateTime.now());
        if (message.getType() == null) {
            message.setType(Message.MessageType.CHAT);
        }
        return messageRepository.save(message);
    }
    
    

    @Transactional
    public Message startNewChat(User sender, User recipient, String content) {
        return sendMessage(sender, recipient, content);
    }

    public List<ConversationPreview> getConversationPreviews(User user) {
        List<ConversationPreview> previews = new ArrayList<>();
        
        // 個人チャットのプレビューを取得
        List<Message> personalMessages = messageRepository.findMessagesForUser(user);
        for (Message message : personalMessages) {
            if (message.getSender() == null || message.getRecipient() == null) {
                continue; // NULL値をスキップ
            }
            User otherUser = message.getSender().getId().equals(user.getId()) ? message.getRecipient() : message.getSender();
            ConversationPreview preview = new ConversationPreview(otherUser, message, false);
            preview.setOtherUser(otherUser);
            preview.setLastMessage(message);
            preview.setGroupChat(false);
            preview.setHasUnread(messageRepository.countUnreadMessagesForConversation(user, otherUser) > 0);
            previews.add(preview);
        }
        
        // グループチャットのプレビューを取得
        List<GroupChat> userGroupChats = groupChatRepository.findByMembersContaining(user);
        for (GroupChat groupChat : userGroupChats) {
            Message lastMessage = messageRepository.findTopByGroupChatOrderBySentAtDesc(groupChat);
            if (lastMessage != null) {
                ConversationPreview preview = new ConversationPreview(user, lastMessage, true);
                preview.setId(groupChat.getId());
                preview.setName(groupChat.getName());
                preview.setLastMessage(lastMessage);
                preview.setGroupChat(true);
                preview.setHasUnread(messageRepository.countUnreadMessagesForGroupChat(groupChat, user) > 0);
                previews.add(preview);
            }
        }
        
        // 最新のメッセージ順にソート
        previews.sort((a, b) -> b.getLastMessage().getSentAt().compareTo(a.getLastMessage().getSentAt()));
        
        return previews;
    }

    public List<Message> searchMessages(User user, String query) {
        return messageRepository.searchMessages(user, query);
    }

    public GroupChat getGroupChatById(Long groupId) {
        return groupChatRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group chat not found"));
    }

    @Transactional
    public GroupChat createGroupChat(String name, User creator, List<User> members) {
        GroupChat groupChat = new GroupChat();
        groupChat.setName(name);
        groupChat.setCreator(creator);
        groupChat.setMembers(members);
        return groupChatRepository.save(groupChat);
    }
    
//    public List<Message> getProjectMessages(Project project) {
//        return messageRepository.findByProjectOrderBySentAtAsc(project);
//    }
//
//    @Transactional
//    public Message sendProjectMessage(Project project, User sender, String content) {
//        Message message = new Message();
//        message.setProject(project);
//        message.setSender(sender);
//        message.setContent(content);
//        message.setSentAt(LocalDateTime.now());
//        message.setType(Message.MessageType.CHAT);
//        return messageRepository.save(message);
//    }
    
    public List<Message> getProjectMessages(Project project) {
        return messageRepository.findByProjectOrderBySentAtAsc(project);
    }

    public Message sendProjectMessage(Project project, User sender, String content) {
        Message message = new Message();
        message.setProject(project);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setType(Message.MessageType.CHAT);
        return messageRepository.save(message);
    }
}