package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.Comment;
import com.socialmedia.post.Social.Media.Project.repository.CommentRepository;
import com.socialmedia.post.Social.Media.Project.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardrailService {
    private final RedisViralityService redisViralityService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private static final int MAX_BOT_REPLIES = 100;
    private static final int MAX_DEPTH = 20;
    private static final long COOLDOWN_SECONDS = 10 * 60; // 10 minutes


    public void checkHorizontalCapAtomic(Long postId) {
        boolean allowed = redisViralityService.tryIncrementBotCount(postId, MAX_BOT_REPLIES);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Post has reached maximum of " + MAX_BOT_REPLIES + " bot replies");
        }
    }

    public void checkVerticalCap(Long parentCommentId) {
        if (parentCommentId == null) return; // reply to post: depth 1 is allowed
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));
        if (parent.getDepthLevel() + 1 > MAX_DEPTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Comment thread exceeds maximum depth of " + MAX_DEPTH);
        }
    }

    public void checkBotHumanCooldown(Long botId, Long humanId) {
        if (redisViralityService.isCooldownPresent(botId, humanId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bot cannot interact with same human more than once per 10 minutes");
        }
        if (!redisViralityService.checkAndSetCooldown(botId, humanId, COOLDOWN_SECONDS)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Cooldown active for this bot-human pair");
        }
    }
}