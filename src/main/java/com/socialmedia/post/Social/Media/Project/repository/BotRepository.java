package com.socialmedia.post.Social.Media.Project.repository;

import com.socialmedia.post.Social.Media.Project.entity.Bot;
import com.socialmedia.post.Social.Media.Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<Bot, Long> {
    Optional<User> findByName(String username);
}