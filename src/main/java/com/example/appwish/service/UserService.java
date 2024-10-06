package com.example.appwish.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.repository.UserRepository;
import com.example.appwish.repository.project.ProjectRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       ProjectRepository projectRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.mailSender = mailSender;
    }

    // User registration and management methods

    public User registerUser(User user) {
        if (isUsernameTaken(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (isEmailTaken(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

//    public User updateUser(User user) {
//        User existingUser = userRepository.findById(user.getId())
//                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
//        
//        if (!existingUser.getUsername().equals(user.getUsername()) && isUsernameTaken(user.getUsername())) {
//            throw new RuntimeException("Username already exists");
//        }
//        if (!existingUser.getEmail().equals(user.getEmail()) && isEmailTaken(user.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//        
//        existingUser.setUsername(user.getUsername());
//        existingUser.setEmail(user.getEmail());
//        existingUser.setUserType(user.getUserType());
//        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
//            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//        existingUser.setUpdatedAt(LocalDateTime.now());
//        return userRepository.save(existingUser);
//    }
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        if (!existingUser.getUsername().equals(user.getUsername()) && isUsernameTaken(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (!existingUser.getEmail().equals(user.getEmail()) && isEmailTaken(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setUserType(user.getUserType());
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }
    
    
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    

  

    // User retrieval methods

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

//    public User getCurrentUser(Authentication authentication) {
//        if (authentication == null) {
//            throw new IllegalStateException("No authentication found");
//        }
//        String username = authentication.getName();
//        return userRepository.findByUsername(username)
//            .orElseThrow(() -> new RuntimeException("Logged in user not found in the database: " + username));
//    }
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Logged in user not found in the database: " + username));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    // Project related methods

    @Transactional(readOnly = true)
    public List<Project> getProjectsByUser(User user) {
        return projectRepository.findByCreatedBy(user);
    }

    // Contact related methods

    @Transactional(readOnly = true)
    public List<User> getContacts(User user) {
        return user.getContacts().stream()
                .filter(contact -> !contact.isDeleted())
                .collect(Collectors.toList());
    }

    public void addContact(User user, User contact) {
        if (!user.getContacts().contains(contact)) {
            user.getContacts().add(contact);
            userRepository.save(user);
        }
    }

    public void removeContact(User user, User contact) {
        if (user.getContacts().contains(contact)) {
            user.getContacts().remove(contact);
            userRepository.save(user);
        }
    }

    // Availability check methods

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // Password reset methods

    public void sendPasswordResetEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("指定されたメールアドレスのユーザーが見つかりません。"));

        String token = generatePasswordResetToken();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String resetUrl = appUrl + "/users/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("パスワードリセットのご案内");
        message.setText("以下のURLからパスワードをリセットしてください：\n" + resetUrl);

        mailSender.send(message);
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    public User findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        User user = findByPasswordResetToken(token);
        if (user == null || user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new Exception("無効または期限切れのトークンです。");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryDate(null);
        userRepository.save(user);
    }
}