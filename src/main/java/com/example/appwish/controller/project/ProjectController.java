package com.example.appwish.controller.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.appwish.service.message.MessageService;
import com.example.appwish.service.project.ProjectService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final MessageService messageService; 

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService, MessageService messageService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping
    public String listProjects(@RequestParam(required = false) String keyword, 
                               @RequestParam(required = false) ProjectCategory category,
                               Model model) {
        List<Project> projects;
        if (keyword != null && !keyword.isEmpty() && category != null) {
            projects = projectService.searchProjectsByKeywordAndCategory(keyword, category);
        } else if (keyword != null && !keyword.isEmpty()) {
            projects = projectService.searchProjects(keyword);
        } else if (category != null) {
            projects = projectService.getProjectsByCategory(category);
        } else {
            projects = projectService.getAllProjects();
        }
        model.addAttribute("projects", projects);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", ProjectCategory.values());
        return "project/list";
    }

//    @GetMapping("/{id}")
//    public String projectDetails(@PathVariable Long id, Model model) {
//        Project project = projectService.getProjectById(id);
//        model.addAttribute("project", project);
//        model.addAttribute("currentUser", getCurrentUser());
//        return "project/details";
//    }
    @GetMapping("/{id}")
    public String projectDetails(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser();
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthorized", isAuthorized(currentUser, project));
        return "project/details";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("categories", ProjectCategory.values());
        model.addAttribute("inputTypes", List.of("structured", "freeform", "both"));
        return "project/form";
    }

    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute Project project, 
                                @RequestParam String inputType,
                                BindingResult result, 
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "project/form";
        }

        User currentUser = getCurrentUser();
        project.setCreatedBy(currentUser);

        if ("freeform".equals(inputType) || "both".equals(inputType)) {
            project.setDescription(project.getFreeformIdea());
        }

        // 構造化フォームのデータを設定
        if ("structured".equals(inputType) || "both".equals(inputType)) {
            // これらのフィールドは既にProjectオブジェクトに設定されているため、
            // 明示的に設定し直す必要はありません。
        }

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に作成されました。");
        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser();
        
        if (!isAuthorized(currentUser, project)) {
            return "redirect:/projects/" + id + "?error=unauthorized";
        }
        
        model.addAttribute("project", project);
        model.addAttribute("categories", ProjectCategory.values());
        model.addAttribute("inputTypes", List.of("structured", "freeform", "both"));
        return "project/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProject(@PathVariable Long id, 
                                @Valid @ModelAttribute Project project, 
                                @RequestParam String inputType,
                                BindingResult result, 
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "project/form";
        }

        Project existingProject = projectService.getProjectById(id);
        User currentUser = getCurrentUser();

        if (!isAuthorized(currentUser, existingProject)) {
            redirectAttributes.addFlashAttribute("error", "このプロジェクトを編集する権限がありません。");
            return "redirect:/projects/" + id;
        }

        if ("freeform".equals(inputType) || "both".equals(inputType)) {
            project.setDescription(project.getFreeformIdea());
        }

        project.setId(id);
        project.setCreatedBy(existingProject.getCreatedBy()); // 作成者情報を維持
        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に更新されました。");
        return "redirect:/projects";
    }

    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser();

        if (!isAuthorized(currentUser, project)) {
            redirectAttributes.addFlashAttribute("error", "このプロジェクトを削除する権限がありません。");
            return "redirect:/projects/" + id;
        }

        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に削除されました。");
        return "redirect:/projects";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    private boolean isAuthorized(User user, Project project) {
        return user != null && project.getCreatedBy() != null && 
               user.getId().equals(project.getCreatedBy().getId());
    }
    
    
    @GetMapping("/{id}/chat")
    public String projectChat(@PathVariable Long id, Model model) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser();
        
        
        List<com.example.appwish.model.message.Message> messages = messageService.getProjectMessages(project);
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("messages", messages);
        return "project/chat";
    }
    
//    @GetMapping("/{id}/chat")
//    public String projectChat(@PathVariable Long id, Model model, Authentication authentication) {
//        User currentUser = userService.getCurrentUser(authentication);
//        if (currentUser == null) {
//            return "redirect:/login";
//        }
//        Project project = projectService.getProjectById(id);
//        List<com.example.appwish.model.message.Message> messages = messageService.getProjectMessages(project);
//        model.addAttribute("project", project);
//        model.addAttribute("messages", messages);
//        model.addAttribute("currentUser", currentUser);
//        return "project/chat";
//    }
    
    
    @PostMapping("/{id}/chat/send")
    public String sendChatMessage(@PathVariable Long id, @RequestParam String content, RedirectAttributes redirectAttributes) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser();
        
        if (!project.getEngineers().contains(currentUser) && !isAuthorized(currentUser, project)) {
            redirectAttributes.addFlashAttribute("error", "メッセージを送信する権限がありません。");
            return "redirect:/projects/" + id;
        }
        
        messageService.sendProjectMessage(project, currentUser, content);
        return "redirect:/projects/" + id + "/chat";
    }
}