package com.example.appwish.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectCategory;
import com.example.appwish.service.UserService;
import com.example.appwish.service.project.ProjectService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                               BindingResult bindingResult, 
                               @RequestParam String confirmPassword, 
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "パスワードと確認用パスワードが一致しません。");
            return "register";
        }

        try {
            // ユーザー名の重複チェック
            if (userService.isUsernameTaken(user.getUsername())) {
                model.addAttribute("errorMessage", "このユーザー名は既に使用されています。");
                return "register";
            }
            
            // メールアドレスの重複チェック
            if (userService.isEmailTaken(user.getEmail())) {
                model.addAttribute("errorMessage", "このメールアドレスは既に登録されています。");
                return "register";
            }

            userService.registerUser(user);
            return "redirect:/users/register/complete";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "登録に失敗しました: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/register/complete")
    public String registerUser(@ModelAttribute("user") User user, Model model, RedirectAttributes redirectAttributes) {
        try {
            // ユーザー名の重複チェック
            if (userService.isUsernameTaken(user.getUsername())) {
                model.addAttribute("errorMessage", "このユーザー名は既に使用されています。");
                return "registerConfirm";
            }
            
            // メールアドレスの重複チェック
            if (userService.isEmailTaken(user.getEmail())) {
                model.addAttribute("errorMessage", "このメールアドレスは既に登録されています。");
                return "registerConfirm";
            }

            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("message", "ユーザー登録が完了しました。");
            return "redirect:/users/register/complete";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "登録に失敗しました: " + e.getMessage());
            return "registerConfirm";
        }
    }

    @GetMapping("/register/complete")
    public String showRegistrationComplete() {
        return "registerComplete";
    }

//    @GetMapping("/profile")
//    public String showProfile(@RequestParam(required = false) String keyword,
//                              @RequestParam(required = false) ProjectCategory category,
//                              Model model, 
//                              Authentication authentication) {
//        if (authentication != null) {
//            String username = authentication.getName();
//            User user = userService.findByUsername(username);
//            List<Project> projects = projectService.getProjectsByUser(user, keyword, category);
//            model.addAttribute("user", user);
//            model.addAttribute("projects", projects);
//            model.addAttribute("keyword", keyword);
//            model.addAttribute("selectedCategory", category);
//            return "user/profile";
//        }
//        return "redirect:/login";
//    }
    
    @GetMapping("/profile")
    public String showProfile(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) ProjectCategory category,
                              Model model,
                              Authentication authentication) {
        if (authentication != null) {
        	String username = authentication.getName();
            User user = userService.getCurrentUser(authentication);
            List<Project> projects = projectService.getProjectsByUser(user, keyword, category);
            model.addAttribute("user", user);
            model.addAttribute("projects", projects);
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedCategory", category);
            return "user/profile";
        }
        return "redirect:/login";
    }
    

    @GetMapping("/edit/{username}")
    public String showEditForm(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "user/userEdit";
    }

    @PostMapping("/edit/{username}")
    public String updateUser(@PathVariable String username, @ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.findByUsername(username);
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("message", "User updated successfully");
            return "redirect:/users/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
            return "redirect:/users/edit/" + username;
        }
    }
    

    
    @PostMapping("/edit")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "user/userEdit";
        }

        try {
            User currentUser = userService.getCurrentUser(authentication);
            
            // 現在のユーザーと異なるユーザー名が既に存在するかチェック
            if (!currentUser.getUsername().equals(user.getUsername()) && userService.isUsernameTaken(user.getUsername())) {
                bindingResult.rejectValue("username", "error.user", "このユーザー名は既に使用されています。");
            }
            
            // 現在のユーザーと異なるメールアドレスが既に存在するかチェック
            if (!currentUser.getEmail().equals(user.getEmail()) && userService.isEmailTaken(user.getEmail())) {
                bindingResult.rejectValue("email", "error.user", "このメールアドレスは既に登録されています。");
            }
            
            if (bindingResult.hasErrors()) {
                return "user/userEdit";
            }
            
//            user.setId(currentUser.getId()); // 現在のユーザーIDを設定
            user.setUsername(user.getUsername());
            user.setEmail(user.getEmail());
            userService.updateUser(user);
            
            // 認証情報を更新
            Authentication newAuth = new UsernamePasswordAuthenticationToken(updateUser(null, null, null), authentication.getCredentials(), authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
            
            redirectAttributes.addFlashAttribute("message", "ユーザー情報が正常に更新されました。");
            return "redirect:/users/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "更新に失敗しました: " + e.getMessage());
            return "user/userEdit";
        }
    }
    
    
    
    @RequestMapping("/api")
    public class UserApiController {

        @Autowired
        private UserService userService;

        @GetMapping("/current-user")
        public User getCurrentUser(Authentication authentication) {
            return userService.getCurrentUser(authentication);
        }
    }
    

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    @GetMapping("/passwordforget")
    public String passwordForget() {
        return "passwordForget";
    }
    
    @PostMapping("/passwordforget")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            userService.sendPasswordResetEmail(email);
            redirectAttributes.addFlashAttribute("success", "パスワードリセットのメールを送信しました。メールをご確認ください。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "メールの送信に失敗しました: " + e.getMessage());
        }
        return "redirect:/users/passwordforget";
    }
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.findByPasswordResetToken(token);
        if (user == null || user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "無効または期限切れのトークンです。");
            return "passwordReset";
        }
        model.addAttribute("token", token);
        return "passwordReset";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("success", "パスワードが正常にリセットされました。");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "パスワードのリセットに失敗しました: " + e.getMessage());
            return "redirect:/users/reset-password?token=" + token;
        }
    }
    
}