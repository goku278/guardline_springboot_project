package com.socialmedia.post.Social.Media.Project.repository;

import com.socialmedia.post.Social.Media.Project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findById(Long id);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.botAuthorId IS NOT NULL")
    long countBotRepliesByPostId(@Param("postId") Long postId);
}