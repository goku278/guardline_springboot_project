package com.socialmedia.post.Social.Media.Project.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotNull
    private AuthorType authorType;
    
    @NotNull
    private Long authorId;
    
    @NotBlank
    private String content;
    
    private Long parentCommentId;
}