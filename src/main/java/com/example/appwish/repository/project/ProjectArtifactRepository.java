package com.example.appwish.repository.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;

@Repository
public interface ProjectArtifactRepository extends JpaRepository<ProjectArtifact, Long> {
    List<ProjectArtifact> findByProjectOrderByUploadedAtDesc(Project project);
    List<ProjectArtifact> findByFavoritedByContaining(User user);
    
    @Query("SELECT a FROM ProjectArtifact a LEFT JOIN FETCH a.comments WHERE a.id = :id")
    ProjectArtifact findByIdWithComments(@Param("id") Long id);
}