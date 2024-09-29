package com.example.appwish.service.project;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.appwish.model.User;
import com.example.appwish.model.project.ArtifactComment;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;
import com.example.appwish.model.project.ProjectCategory;
import com.example.appwish.repository.project.ArtifactCommentRepository;
import com.example.appwish.repository.project.ProjectArtifactRepository;
import com.example.appwish.repository.project.ProjectRepository;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectArtifactRepository projectArtifactRepository;
    private final ArtifactCommentRepository artifactCommentRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, 
                          ProjectArtifactRepository projectArtifactRepository,
                          ArtifactCommentRepository artifactCommentRepository) {
        this.projectRepository = projectRepository;
        this.projectArtifactRepository = projectArtifactRepository;
        this.artifactCommentRepository = artifactCommentRepository;
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
    @Transactional
    public boolean toggleProjectFavorite(Project project, User user) {
        if (project.isFavoritedBy(user)) {
            project.removeFavorite(user);
            return false;
        } else {
            project.addFavorite(user);
            return true;
        }
    }

    public boolean isFavorited(Long artifactId, User user) {
        ProjectArtifact artifact = getProjectArtifactById(artifactId);
        return artifact.isFavoritedBy(user);
    }


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
    
    public List<Project> getProjectsByUser(User user, String keyword, ProjectCategory category) {
        List<Project> userProjects = projectRepository.findByCreatedBy(user);
        
        if (keyword == null && category == null) {
            return userProjects;
        }
        
        return userProjects.stream()
            .filter(project -> 
                (keyword == null || project.getTitle().contains(keyword) || project.getDescription().contains(keyword)) &&
                (category == null || project.getCategory() == category)
            )
            .collect(Collectors.toList());
    }
    
    public ProjectArtifact getProjectArtifactWithComments(Long id) {
        return projectArtifactRepository.findByIdWithComments(id);
    }

    @Transactional


    public List<ArtifactComment> getArtifactComments(Long artifactId) {
        ProjectArtifact artifact = getProjectArtifactById(artifactId);
        return artifact.getComments();
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        ArtifactComment comment = artifactCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        if (!comment.getUser().equals(user)) {
            throw new RuntimeException("User is not authorized to delete this comment");
        }
        
        ProjectArtifact artifact = comment.getArtifact();
        artifact.removeComment(comment);
        projectArtifactRepository.save(artifact);
        artifactCommentRepository.delete(comment);
    }

    @Transactional
    public ArtifactComment updateComment(Long commentId, String newContent, User user) {
        ArtifactComment comment = artifactCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        if (!comment.getUser().equals(user)) {
            throw new RuntimeException("User is not authorized to update this comment");
        }
        
        comment.setContent(newContent);
        return artifactCommentRepository.save(comment);
    }
    
    public List<ArtifactComment> getLatestComments(ProjectArtifact artifact, int limit) {
        return artifactCommentRepository.findLatestComments(artifact, PageRequest.of(0, limit));
    }
    
    @Transactional
    public ArtifactComment addCommentToArtifact(Long artifactId, Long projectId, String content, User user) {
        ProjectArtifact artifact = getProjectArtifactById(artifactId);
        Project project = getProjectById(projectId);
        ArtifactComment comment = new ArtifactComment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setArtifact(artifact);
        comment.setProject(project);
        artifactCommentRepository.save(comment);
        artifact.addComment(comment);
        projectArtifactRepository.save(artifact);
        return comment;
    }
    
    

}