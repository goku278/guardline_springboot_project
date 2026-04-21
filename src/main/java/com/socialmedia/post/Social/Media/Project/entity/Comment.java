package com.socialmedia.post.Social.Media.Project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "user_author_id")
    private Long userAuthorId;

    @Column(name = "bot_author_id")
    private Long botAuthorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "depth_level", nullable = false)
    private Integer depthLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}