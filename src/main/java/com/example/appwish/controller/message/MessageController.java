package com.example.appwish.controller.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appwish.model.User;
import com.example.appwish.model.message.GroupChat;
import com.example.appwish.model.message.Message;
import com.example.appwish.service.UserService;
import com.example.appwish.service.message.ConversationPreview;
import com.example.appwish.service.message.MessageService;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

//    @GETMAPPING("/LIST")
//    PUBLIC STRING SHOWMESSAGELIST(MODEL MODEL, AUTHENTICATION AUTHENTICATION) {
//        USER CURRENTUSER = USERSERVICE.GETCURRENTUSER(AUTHENTICATION);
//        IF (CURRENTUSER == NULL) {
//            RETURN "REDIRECT:/LOGIN";
//        }
//        LIST<CONVERSATIONPREVIEW> CONVERSATIONS = MESSAGESERVICE.GETCONVERSATIONPREVIEWS(CURRENTUSER);
//        MODEL.ADDATTRIBUTE("CONVERSATIONS", CONVERSATIONS);
//        MODEL.ADDATTRIBUTE("CURRENTUSER", CURRENTUSER);
//        RETURN "MESSAGES/LIST";
//    }
    @GetMapping("/list")
    public String showMessageList(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String chatType,
                                  Model model,
                                  Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<ConversationPreview> conversations = messageService.getConversationPreviews(currentUser, keyword, chatType);
        model.addAttribute("conversations", conversations);
        model.addAttribute("keyword", keyword);
        model.addAttribute("chatType", chatType);
        model.addAttribute("currentUser", currentUser);
        return "messages/list";
    }
    

    @GetMapping("/private/{recipientId}")
    public String showPrivateChat(@PathVariable Long recipientId, Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        User recipient = userService.getUserById(recipientId);
        List<Message> conversation = messageService.getConversation(currentUser, recipient);
        model.addAttribute("conversation", conversation);
        model.addAttribute("recipient", recipient);
        model.addAttribute("currentUser", currentUser);
        return "messages/conversation";
    }

    @GetMapping("/group/{groupId}")
    public String showGroupChat(@PathVariable Long groupId, Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        GroupChat groupChat = messageService.getGroupChatById(groupId);
        List<Message> conversation = messageService.getGroupConversation(groupId);
        model.addAttribute("conversation", conversation);
        model.addAttribute("groupChat", groupChat);
        model.addAttribute("currentUser", currentUser);
        return "messages/groupChat";
    }

    @GetMapping("/new")
    public String showNewChatForm(Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<User> allUsers = userService.getAllUsers();
        allUsers.remove(currentUser);
        model.addAttribute("users", allUsers);
        model.addAttribute("currentUser", currentUser);
        return "messages/newChat";
    }

    @PostMapping("/private/start")
    public String startPrivateChat(@RequestParam Long recipientId, @RequestParam String content, Authentication authentication) {
        User sender = userService.getCurrentUser(authentication);
        if (sender == null) {
            return "redirect:/login";
        }
        User recipient = userService.getUserById(recipientId);
        messageService.sendMessage(sender, recipient, content);
        return "redirect:/messages/conversation/" + recipientId;
    }

    @GetMapping("/group/create")
    public String showCreateGroupForm(Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<User> allUsers = userService.getAllUsers();
        allUsers.remove(currentUser);
        model.addAttribute("users", allUsers);
        model.addAttribute("currentUser", currentUser);
        return "messages/createGroupChat";
    }

    @PostMapping("/group/create")
    public String createGroupChat(@RequestParam String groupName, @RequestParam List<Long> memberIds, Authentication authentication) {
        User creator = userService.getCurrentUser(authentication);
        if (creator == null) {
            return "redirect:/login";
        }
        List<User> members = userService.getUsersByIds(memberIds);
        members.add(creator);
        GroupChat groupChat = messageService.createGroupChat(groupName, creator, members);
        return "redirect:/messages/group/" + groupChat.getId();
    }

    @GetMapping("/conversation/{recipientId}")
    public String showConversation(@PathVariable Long recipientId, Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        User recipient = userService.getUserById(recipientId);
        List<Message> conversation = messageService.getConversation(currentUser, recipient);
        model.addAttribute("conversation", conversation);
        model.addAttribute("recipient", recipient);
        model.addAttribute("currentUser", currentUser);
        return "messages/conversation";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam Long recipientId, @RequestParam String content, Authentication authentication) {
        User sender = userService.getCurrentUser(authentication);
        if (sender == null) {
            return "redirect:/login";
        }
        User recipient = userService.getUserById(recipientId);
        messageService.sendMessage(sender, recipient, content);
        return "redirect:/messages/conversation/" + recipientId;
    }
    
    @GetMapping("/chatrelate")
    public String showChatrelate() {
        return "messages/chatrelate";
    }
    

    @PostMapping("/delete")
    public String deleteConversation(@RequestParam Long conversationId, 
                                     @RequestParam boolean isGroupChat, 
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            if (isGroupChat) {
                messageService.deleteGroupChat(conversationId);
            } else {
                messageService.deleteConversation(currentUser.getId(), conversationId);
            }
            redirectAttributes.addFlashAttribute("message", "チャットが正常に削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "チャットの削除に失敗しました: " + e.getMessage());
        }
        return "redirect:/messages/list";
    }
}