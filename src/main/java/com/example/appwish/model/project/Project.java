package com.example.appwish.model.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.appwish.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_provider_id")
    private User ideaProvider;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_engineers",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> engineers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.PROPOSED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectCategory category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "idea_seed")
    private String ideaSeed;

    @Column(name = "problem_statement")
    private String problem;

    @Column(name = "ideal_situation")
    private String idealSituation;

    @Column(name = "current_solution")
    private String currentSolution;

    @Column(name = "user_story")
    private String userStory;

    @Column(name = "emotional_connection")
    private String emotionalConnection;
    
    @ElementCollection
    @CollectionTable(name = "project_key_features", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "feature")
    private List<String> keyFeatures = new ArrayList<>();
    
    @Column(name = "usability_point")
    private String usabilityPoint;

    @Column(name = "future_possibility")
    private String futurePossibility;

    @Column(name = "appeal_point")
    private String appealPoint;

    @Column(name = "freeform_idea")
    @Lob
    private String freeformIdea;

    @Column(name = "input_type")
    private String inputType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // inputType のゲッターとセッター
    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    

    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectArtifact> artifacts = new ArrayList<>();

    public void addArtifact(ProjectArtifact artifact) {
        artifacts.add(artifact);
        artifact.setProject(this);
    }

    public void removeArtifact(ProjectArtifact artifact) {
        artifacts.remove(artifact);
        artifact.setProject(null);
    }
    
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_favorites",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> favoritedBy = new HashSet<>();
    
    public void addFavorite(User user) {
        favoritedBy.add(user);
    }

    public void removeFavorite(User user) {
        favoritedBy.remove(user);
    }

    public boolean isFavoritedBy(User user) {
        return favoritedBy.contains(user);
    }
    
    @Column(name = "image_url")
    private String imageUrl;

    // getter and setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
}