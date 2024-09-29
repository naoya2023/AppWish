package com.example.appwish.service.project;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.appwish.model.User;
import com.example.appwish.model.project.Project;
import com.example.appwish.model.project.ProjectArtifact;
import com.example.appwish.repository.project.ProjectArtifactRepository;

@Service
public class ProjectArtifactService {

    @Autowired
    private ProjectArtifactRepository repository;

    public void saveArtifact(Project project, String description, MultipartFile file, User uploadedBy) throws IOException {
        // Save file to storage and get file path or URL
        String filename = file.getOriginalFilename(); // Simplified for demonstration

        // Create and save ProjectArtifact entity
        ProjectArtifact artifact = new ProjectArtifact();
        artifact.setProject(project);
        artifact.setDescription(description);
        artifact.setFilename(filename);
//        artifact.setStatus("Uploaded");
        artifact.setUploadedBy(uploadedBy);
        repository.save(artifact);
    }
}