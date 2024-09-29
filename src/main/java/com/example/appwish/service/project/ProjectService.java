package com.example.appwish.service.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;
import com.example.appwish.model.project.ProjectCategory;
import com.example.appwish.repository.project.ProjectArtifactRepository;
import com.example.appwish.repository.project.ProjectRepository;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectArtifactRepository projectArtifactRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectArtifactRepository projectArtifactRepository) {
        this.projectRepository = projectRepository;
        this.projectArtifactRepository = projectArtifactRepository;
    }
    
     public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjects(String keyword, ProjectCategory category) {
        if (keyword != null && !keyword.isEmpty() && category != null) {
            return projectRepository.findByTitleContainingOrDescriptionContainingAndCategory(keyword, keyword, category);
        } else if (keyword != null && !keyword.isEmpty()) {
            return projectRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
        } else if (category != null) {
            return projectRepository.findByCategory(category);
        } else {
            return projectRepository.findAll();
        }
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

    public Project getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        if (project.getCreatedBy() == null) {
            throw new RuntimeException("Project creator is not set for project with id: " + id);
        }
        return project;
    }

    @Transactional
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
    
    public ProjectArtifact saveProjectArtifact(ProjectArtifact artifact) {
        return projectArtifactRepository.save(artifact);
    }

    public List<ProjectArtifact> getProjectArtifacts(Project project) {
        return projectArtifactRepository.findByProjectOrderByUploadedAtDesc(project);
    }
    
    public ProjectArtifact getProjectArtifactById(Long id) {
        return projectArtifactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Artifact not found with id: " + id));
    }

    @Transactional
    public boolean toggleFavorite(Long artifactId, User user) {
        ProjectArtifact artifact = getProjectArtifactById(artifactId);
        if (artifact.isFavoritedBy(user)) {
            artifact.removeFavorite(user);
            projectArtifactRepository.save(artifact);
            return false;
        } else {
            artifact.addFavorite(user);
            projectArtifactRepository.save(artifact);
            return true;
        }
    }

    public boolean isFavorited(Long artifactId, User user) {
        ProjectArtifact artifact = getProjectArtifactById(artifactId);
        return artifact.isFavoritedBy(user);
    }

//    public List<ProjectArtifact> getFavoriteArtifacts(User user) {
//        return projectArtifactRepository.findByFavoritedByContaining(user);
//    }
    
//    public List<Project> getFavoriteProjects(User user) {
//        return projectRepository.findByFavoritedByContaining(user);
//    }
//    
    public List<ProjectArtifact> getFavoriteArtifacts(User user) {
        List<ProjectArtifact> artifacts = projectArtifactRepository.findByFavoritedByContaining(user);
        for (ProjectArtifact artifact : artifacts) {
            artifact.getProject().getTitle(); // This will force the loading of the project
        }
        return artifacts;
    }
    
    @Transactional
    public boolean toggleFavorite(Project project, User user) {
        if (project.isFavoritedBy(user)) {
            project.removeFavorite(user);
            return false;
        } else {
            project.addFavorite(user);
            return true;
        }
    }

    public List<Project> getFavoriteProjects(User user) {
        return projectRepository.findByFavoritedByContaining(user);
    }


}