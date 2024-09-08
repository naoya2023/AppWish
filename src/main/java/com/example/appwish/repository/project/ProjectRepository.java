package com.example.appwish.repository.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectCategory;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTitleContainingOrDescriptionContaining(String title, String description);
    List<Project> findByCategory(ProjectCategory category);
    List<Project> findByTitleContainingOrDescriptionContainingAndCategory(String title, String description, ProjectCategory category);
}