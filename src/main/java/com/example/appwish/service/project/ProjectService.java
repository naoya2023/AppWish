package com.example.appwish.service.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectCategory;
import com.example.appwish.repository.project.ProjectRepository;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> searchProjects(String keyword) {
        return projectRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    public List<Project> getProjectsByCategory(ProjectCategory category) {
        return projectRepository.findByCategory(category);
    }

    public List<Project> searchProjectsByKeywordAndCategory(String keyword, ProjectCategory category) {
        return projectRepository.findByTitleContainingOrDescriptionContainingAndCategory(keyword, keyword, category);
    }

//    public Project getProjectById(Long id) {
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
//        if (project.getCreatedBy() != null) {
//            // 作成者の情報を確実に取得
//            project.getCreatedBy().getUsername();
//        }
//        return project;
//    }
    
    public Project getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        // createdBy が null でないことを確認
        if (project.getCreatedBy() == null) {
            throw new RuntimeException("Project creator is not set for project with id: " + id);
        }
        return project;
    }
    

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}