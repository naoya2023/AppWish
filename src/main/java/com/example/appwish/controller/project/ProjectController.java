package com.example.appwish.controller.project;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appwish.model.User;
import com.example.appwish.model.message.Message;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;
import com.example.appwish.model.project.ProjectCategory;
import com.example.appwish.service.UserService;
import com.example.appwish.service.message.MessageService;
import com.example.appwish.service.project.ProjectService;
import com.example.appwish.service.project.StorageService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final MessageService messageService;
    private final StorageService storageService;

    @Autowired
    public ProjectController(ProjectService projectService, UserService userService, MessageService messageService, StorageService storageService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService = messageService;
        this.storageService = storageService;
    }

    @GetMapping
    public String listProjects(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) ProjectCategory category,
                               Model model,
                               Authentication authentication) {
        List<Project> projects = projectService.getProjects(keyword, category);
        model.addAttribute("projects", projects);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", ProjectCategory.values());
        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName());
            model.addAttribute("currentUser", currentUser);
        }
        return "project/list";
    }

    @GetMapping("/{id}")
    public String projectDetails(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthorized", isAuthorized(currentUser, project));
        if (project.getKeyFeatures() != null && !project.getKeyFeatures().isEmpty()) {
            model.addAttribute("hasKeyFeatures", true);
        }
        
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
                                @RequestParam("projectImage") MultipartFile file,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        if (result.hasErrors()) {
            return "project/form";
        }

        User currentUser = getCurrentUser(authentication);
        project.setCreatedBy(currentUser);

        if ("freeform".equals(inputType) || "both".equals(inputType)) {
            project.setDescription(project.getFreeformIdea());
        }

        if (!file.isEmpty()) {
            try {
                String filename = storageService.store(file);
                project.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "画像のアップロードに失敗しました: " + e.getMessage());
                return "redirect:/projects/create";
            }
        }

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に作成されました。");
        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
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
                                @RequestParam("projectImage") MultipartFile file,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        if (result.hasErrors()) {
            return "project/form";
        }

        Project existingProject = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        if (!isAuthorized(currentUser, existingProject)) {
            redirectAttributes.addFlashAttribute("error", "このプロジェクトを編集する権限がありません。");
            return "redirect:/projects/" + id;
        }

        if ("freeform".equals(inputType) || "both".equals(inputType)) {
            project.setDescription(project.getFreeformIdea());
        }

        if (!file.isEmpty()) {
            try {
                String filename = storageService.store(file);
                project.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "画像のアップロードに失敗しました: " + e.getMessage());
                return "redirect:/projects/edit/" + id;
            }
        } else {
            project.setImageUrl(existingProject.getImageUrl());
        }

        project.setId(id);
        project.setCreatedBy(existingProject.getCreatedBy());
        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に更新されました。");
        return "redirect:/projects";
    }

    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        if (!isAuthorized(currentUser, project)) {
            redirectAttributes.addFlashAttribute("error", "このプロジェクトを削除する権限がありません。");
            return "redirect:/projects/" + id;
        }
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に削除されました。");
        return "redirect:/projects";
    }

    @GetMapping("/{id}/artifacts")
    public String listArtifacts(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        List<ProjectArtifact> artifacts = projectService.getProjectArtifacts(project);
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("project", project);
        model.addAttribute("artifacts", artifacts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthorized", isAuthorized(currentUser, project));
        model.addAttribute("projectService", projectService);
        return "project/artifacts";
    }

    @PostMapping("/artifacts/{artifactId}/favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long artifactId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        boolean isFavorited = projectService.toggleFavorite(artifactId, currentUser);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFavorited", isFavorited);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public String listFavorites(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Project> favoriteProjects = projectService.getFavoriteProjects(currentUser);
        List<ProjectArtifact> favoriteArtifacts = projectService.getFavoriteArtifacts(currentUser);
        model.addAttribute("favoriteProjects", favoriteProjects);
        model.addAttribute("favoriteArtifacts", favoriteArtifacts);
        return "project/favoriteProject";
    }

    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Project project = projectService.getProjectById(id);
        boolean isFavorited = projectService.toggleFavorite(project, currentUser);
        String message = isFavorited ? "プロジェクトをお気に入りに追加しました。" : "プロジェクトをお気に入りから削除しました。";
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/projects/" + id;
    }

    @GetMapping("/{id}/chat")
    public String projectChat(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        List<Message> messages = messageService.getProjectMessages(project);
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("messages", messages);
        return "project/chat";
    }

    @PostMapping("/{id}/chat/send")
    public String sendChatMessage(@PathVariable Long id, @RequestParam String content, RedirectAttributes redirectAttributes, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        if (!project.getEngineers().contains(currentUser) && !isAuthorized(currentUser, project)) {
            redirectAttributes.addFlashAttribute("error", "メッセージを送信する権限がありません。");
            return "redirect:/projects/" + id;
        }
        messageService.sendProjectMessage(project, currentUser, content);
        return "redirect:/projects/" + id + "/chat";
    }

    @GetMapping("/projectsrelate")
    public String projectRelate() {
        return "project/projectsrelate";
    }

    @GetMapping("/{id}/upload")
    public String showUploadForm(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.getProjectById(id);
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("project", project);
        return "project/upload";
    }

    @GetMapping("/{projectId}/artifacts/{artifactId}")
    public String artifactDetails(@PathVariable Long projectId, @PathVariable Long artifactId, Model model) {
        Project project = projectService.getProjectById(projectId);
        ProjectArtifact artifact = projectService.getProjectArtifactById(artifactId);
        model.addAttribute("project", project);
        model.addAttribute("artifact", artifact);
        return "project/artifactUpload";
    }

    @PostMapping("/{id}/upload")
    public String handleFileUpload(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam("description") String description,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        try {
            Project project = projectService.getProjectById(id);
            User currentUser = getCurrentUser(authentication);
            String filename = storageService.store(file);
            ProjectArtifact artifact = new ProjectArtifact();
            artifact.setProject(project);
            artifact.setFilename(filename);
            artifact.setDescription(description);
            artifact.setUploadedBy(currentUser);
            artifact.setUploadedAt(LocalDateTime.now());
            project.addArtifact(artifact);
            projectService.saveProject(project);
            redirectAttributes.addFlashAttribute("message", "ファイルが正常にアップロードされました。");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "ファイルのアップロードに失敗しました: " + e.getMessage());
        }
        return "redirect:/projects/" + id + "/artifacts";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        ProjectArtifact artifact = projectService.getProjectArtifactById(id);
        Resource file = storageService.loadAsResource(artifact.getFilename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userService.findByUsername(authentication.getName());
    }

    private boolean isAuthorized(User user, Project project) {
        return user != null && project.getCreatedBy() != null && user.getId().equals(project.getCreatedBy().getId());
    }
}