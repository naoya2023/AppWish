package com.example.appwish.repository.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;

public interface ProjectArtifactRepository extends JpaRepository<ProjectArtifact, Long> {
    List<ProjectArtifact> findByProjectOrderByUploadedAtDesc(Project project);
    List<ProjectArtifact> findByFavoritedByContaining(User user);
    
    
}