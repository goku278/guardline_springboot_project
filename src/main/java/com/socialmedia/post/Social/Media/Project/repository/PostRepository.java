package com.socialmedia.post.Social.Media.Project.repository;

import com.socialmedia.post.Social.Media.Project.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}