package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.User;
import com.socialmedia.post.Social.Media.Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Create User
    public User createUser(User user) {

        // Check duplicate username
        userRepository.findByUsername(user.getUsername())
                .ifPresent(u -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Username already exists"
                    );
                });

        return userRepository.save(user);
    }

    // Get user by ID
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Delete user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}