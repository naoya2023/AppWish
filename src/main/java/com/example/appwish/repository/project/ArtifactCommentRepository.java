package com.example.appwish.repository.project;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.project.ArtifactComment;
import com.example.appwish.model.project.ProjectArtifact;

@Repository
public interface ArtifactCommentRepository extends JpaRepository<ArtifactComment, Long> {

    List<ArtifactComment> findByArtifactOrderByCreatedAtDesc(ProjectArtifact artifact);
    List<ArtifactComment> findByArtifactOrderByCreatedAtAsc(ProjectArtifact artifact);
    List<ArtifactComment> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByArtifact(ProjectArtifact artifact);

    @Query("SELECT ac FROM ArtifactComment ac WHERE ac.artifact = :artifact AND LOWER(ac.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ArtifactComment> searchCommentsByKeyword(@Param("artifact") ProjectArtifact artifact, @Param("keyword") String keyword);

    @Query("SELECT ac FROM ArtifactComment ac WHERE ac.artifact = :artifact AND ac.createdAt BETWEEN :startDate AND :endDate")
    List<ArtifactComment> findCommentsByDateRange(@Param("artifact") ProjectArtifact artifact, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ac FROM ArtifactComment ac WHERE ac.artifact = :artifact ORDER BY ac.createdAt DESC")
    List<ArtifactComment> findLatestComments(@Param("artifact") ProjectArtifact artifact, Pageable pageable);

    @Query("SELECT ac FROM ArtifactComment ac WHERE ac.user.id = :userId ORDER BY ac.createdAt DESC")
    List<ArtifactComment> findLatestCommentsByUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE ArtifactComment ac SET ac.deleted = true WHERE ac.id = :commentId")
    void softDeleteComment(@Param("commentId") Long commentId);

    @Query("SELECT ac FROM ArtifactComment ac WHERE ac.artifact = :artifact AND ac.deleted = false ORDER BY ac.createdAt DESC")
    List<ArtifactComment> findNonDeletedComments(@Param("artifact") ProjectArtifact artifact);
}