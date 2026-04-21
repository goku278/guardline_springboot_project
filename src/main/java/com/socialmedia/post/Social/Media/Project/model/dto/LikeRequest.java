package com.socialmedia.post.Social.Media.Project.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {
    @NotNull
    private Long userId;
}