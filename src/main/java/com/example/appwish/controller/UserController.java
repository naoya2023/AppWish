package com.example.appwish.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
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
    
    @PostMapping("/register/confirm")
    public String confirmRegistration(@ModelAttribute("user") User user, Model model) {
        model.addAttribute("user", user);
        return "registerConfirm";
    }

    @PostMapping("/register/complete")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("message", "ユーザー登録が完了しました。");
            return "redirect:/users/register/complete";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登録に失敗しました: " + e.getMessage());
            return "redirect:/users/register";
        }
    }

    @GetMapping("/register/complete")
    public String showRegistrationComplete() {
        return "registerComplete";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            List<Project> projects = userService.getProjectsByUser(user);
            model.addAttribute("user", user);
            model.addAttribute("projects", projects);
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
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/userEdit";
        }

        try {
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("message", "ユーザー情報が正常に更新されました。");
            return "redirect:/users/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新に失敗しました: " + e.getMessage());
            return "redirect:/users/edit";
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
}