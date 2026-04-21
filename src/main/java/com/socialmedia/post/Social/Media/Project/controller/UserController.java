package com.socialmedia.post.Social.Media.Project.controller;

import com.socialmedia.post.Social.Media.Project.entity.Comment;
import com.socialmedia.post.Social.Media.Project.entity.Post;
import com.socialmedia.post.Social.Media.Project.entity.User;
import com.socialmedia.post.Social.Media.Project.model.dto.CommentCreateRequest;
import com.socialmedia.post.Social.Media.Project.model.dto.PostCreateRequest;
import com.socialmedia.post.Social.Media.Project.service.CommentService;
import com.socialmedia.post.Social.Media.Project.service.PostService;
import com.socialmedia.post.Social.Media.Project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create User
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Get User by ID
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    // Get All Users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Delete User
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}