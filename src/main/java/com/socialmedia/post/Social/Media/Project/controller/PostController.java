package com.socialmedia.post.Social.Media.Project.controller;

import com.socialmedia.post.Social.Media.Project.entity.Comment;
import com.socialmedia.post.Social.Media.Project.entity.Post;
import com.socialmedia.post.Social.Media.Project.model.dto.CommentCreateRequest;
import com.socialmedia.post.Social.Media.Project.model.dto.PostCreateRequest;
import com.socialmedia.post.Social.Media.Project.service.CommentService;
import com.socialmedia.post.Social.Media.Project.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }
    
    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        return commentService.addComment(postId, request);
    }
}