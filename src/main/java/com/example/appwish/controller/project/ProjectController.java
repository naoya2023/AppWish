package com.example.appwish.controller.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.appwish.model.project.Project;
import com.example.appwish.service.project.ProjectService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String listProjects(@RequestParam(required = false) String keyword, Model model) {
        List<Project> projects = (keyword != null && !keyword.isEmpty()) 
            ? projectService.searchProjects(keyword)
            : projectService.getAllProjects();
        model.addAttribute("projects", projects);
        model.addAttribute("keyword", keyword);
        return "project/list";
    }

    @GetMapping("/{id}")
    public String projectDetails(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
        return "project/details";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "project/form";
    }

    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute Project project, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "project/form";
        }
        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に作成されました。");
        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
        return "project/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProject(@PathVariable Long id, @Valid @ModelAttribute Project project, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "project/form";
        }
        project.setId(id);
        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に更新されました。");
        return "redirect:/projects";
    }

    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("message", "プロジェクトが正常に削除されました。");
        return "redirect:/projects";
    }
}