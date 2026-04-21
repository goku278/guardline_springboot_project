package com.socialmedia.post.Social.Media.Project.controller;

import com.socialmedia.post.Social.Media.Project.model.dto.LikeRequest;
import com.socialmedia.post.Social.Media.Project.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likePost(@PathVariable Long postId, @Valid @RequestBody LikeRequest request) {
        likeService.likePost(postId, request.getUserId());
    }
}