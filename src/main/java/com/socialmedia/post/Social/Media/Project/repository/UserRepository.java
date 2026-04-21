package com.socialmedia.post.Social.Media.Project.repository;

import com.socialmedia.post.Social.Media.Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}