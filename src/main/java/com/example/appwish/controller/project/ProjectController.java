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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.appwish.model.User;
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
	public ProjectController(ProjectService projectService, UserService userService, MessageService messageService,
			StorageService storageService) {
		this.projectService = projectService;
		this.userService = userService;
		this.messageService = messageService;
		this.storageService = storageService;
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
	//    private User getCurrentUser() {
	//        // Logic to retrieve current user (e.g., from security context)
	//        return new User(); // Placeholder
	//    }

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

	@PostMapping("/{id}/chat/send")
	public String sendChatMessage(@PathVariable Long id, @RequestParam String content,
			RedirectAttributes redirectAttributes) {
		Project project = projectService.getProjectById(id);
		User currentUser = getCurrentUser();

		if (!project.getEngineers().contains(currentUser) && !isAuthorized(currentUser, project)) {
			redirectAttributes.addFlashAttribute("error", "メッセージを送信する権限がありません。");
			return "redirect:/projects/" + id;
		}

		messageService.sendProjectMessage(project, currentUser, content);
		return "redirect:/projects/" + id + "/chat";
	}

	@GetMapping("/projectsrelate")
	public String projecRelatet() {
		return "project/projectsrelate";
	}

	@GetMapping("/{id}/upload")
	public String showUploadForm(@PathVariable Long id, Model model) {
		Project project = projectService.getProjectById(id);
		User currentUser = getCurrentUser();
		//        if (!isAuthorized(currentUser, project)) {
		//            return "redirect:/projects/" + id + "?error=unauthorized";
		//        }
		model.addAttribute("project", project);
		return "project/upload";
	}

	//    @GetMapping("/{id}/artifacts")
	//    public String listArtifacts(@PathVariable Long id, Model model) {
	//        Project project = projectService.getProjectById(id);
	//        List<ProjectArtifact> artifacts = projectService.getProjectArtifacts(project);
	//        User currentUser = getCurrentUser();
	//        
	//        model.addAttribute("project", project);
	//        model.addAttribute("artifacts", artifacts);
	//        model.addAttribute("isAuthorized", isAuthorized(currentUser, project));
	//        
	//        return "project/artifacts";
	//    }

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
			RedirectAttributes redirectAttributes) {
		try {
			Project project = projectService.getProjectById(id);
			User currentUser = getCurrentUser(); // Assume method to get current user

			String filename = storageService.store(file);

			ProjectArtifact artifact = new ProjectArtifact();
			artifact.setProject(project);
			artifact.setFilename(filename);
			artifact.setDescription(description);
			artifact.setUploadedBy(currentUser);
			artifact.setUploadedAt(LocalDateTime.now());

			project.addArtifact(artifact); // Add artifact to project
			projectService.saveProject(project); // Save project with new artifact

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



	
	@PostMapping("/{id}/favorite")
	public String toggleFavorite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
	    User currentUser = getCurrentUser();
	    Project project = projectService.getProjectById(id);
	    
	    if (project.isFavoritedBy(currentUser)) {
	        project.removeFavorite(currentUser);
	    } else {
	        project.addFavorite(currentUser);
	    }
	    
	    projectService.saveProject(project);
	    redirectAttributes.addFlashAttribute("message", "お気に入りを更新しました。");
	    return "redirect:/projects";
	}
	
	@GetMapping("/{id}/artifacts")
	public String listArtifacts(@PathVariable Long id, Model model) {
	    Project project = projectService.getProjectById(id);
	    List<ProjectArtifact> artifacts = projectService.getProjectArtifacts(project);
	    User currentUser = getCurrentUser();
	    
	    model.addAttribute("project", project);
	    model.addAttribute("artifacts", artifacts);
	    model.addAttribute("currentUser", currentUser);
	    model.addAttribute("isAuthorized", isAuthorized(currentUser, project));
	    model.addAttribute("projectService", projectService);  // この行を追加
	    
	    return "project/artifacts";
	}
	
	
	@PostMapping("/artifacts/{artifactId}/favorite")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long artifactId) {
	    User currentUser = getCurrentUser();
	    boolean isFavorited = projectService.toggleFavorite(artifactId, currentUser);
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("isFavorited", isFavorited);
	    
	    return ResponseEntity.ok(response);
	}

	@GetMapping("/favorites")
	public String listFavoriteArtifacts(Model model) {
	    User currentUser = getCurrentUser();
	    List<ProjectArtifact> favoriteArtifacts = projectService.getFavoriteArtifacts(currentUser);
	    model.addAttribute("favoriteArtifacts", favoriteArtifacts);
	    return "project/favoriteArtifacts";
	}

}