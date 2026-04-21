package com.socialmedia.post.Social.Media.Project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_author_id")
    private Long userAuthorId;

    @Column(name = "bot_author_id")
    private Long botAuthorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userAuthorId=" + userAuthorId +
                ", botAuthorId=" + botAuthorId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}