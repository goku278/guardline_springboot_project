package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.Comment;
import com.socialmedia.post.Social.Media.Project.entity.Post;
import com.socialmedia.post.Social.Media.Project.model.dto.AuthorType;
import com.socialmedia.post.Social.Media.Project.model.dto.CommentCreateRequest;
import com.socialmedia.post.Social.Media.Project.repository.BotRepository;
import com.socialmedia.post.Social.Media.Project.repository.CommentRepository;
import com.socialmedia.post.Social.Media.Project.repository.PostRepository;
import com.socialmedia.post.Social.Media.Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final GuardrailService guardrailService;
    private final RedisViralityService redisViralityService;
    private final NotificationService notificationService;
    
    @Transactional
    public Comment addComment(Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        // Determine depth
        Integer depth;
        Long parentCommentId = request.getParentCommentId();
        Long humanTargetId = null;
        
        if (parentCommentId == null) {
            depth = 1;
            // Target human for notification: post author if user
            if (post.getUserAuthorId() != null) {
                humanTargetId = post.getUserAuthorId();
            }
        } else {
            Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));
            depth = parent.getDepthLevel() + 1;
            // Target human: author of parent comment if user
            if (parent.getUserAuthorId() != null) {
                humanTargetId = parent.getUserAuthorId();
            }
        }
        
        // If author is Bot, apply guardrails
        Long botId = null;
        Long userId = null;
        boolean isBot = request.getAuthorType() == AuthorType.BOT;
        
        if (isBot) {
            botId = request.getAuthorId();
            botRepository.findById(botId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bot not found"));
            
            // 1. Horizontal cap (atomic)
            guardrailService.checkHorizontalCapAtomic(postId);
            
            // 2. Vertical cap
            guardrailService.checkVerticalCap(parentCommentId);
            
            // 3. Cooldown: need humanId from post author (if user) or parent comment author
            Long cooldownHumanId = (post.getUserAuthorId() != null) ? post.getUserAuthorId() : 
                                    (parentCommentId != null ? 
                                     commentRepository.findById(parentCommentId).map(c -> c.getUserAuthorId()).orElse(null) : null);
            if (cooldownHumanId != null) {
                guardrailService.checkBotHumanCooldown(botId, cooldownHumanId);
            }
            
            // Increment virality score for bot reply (+1)
            redisViralityService.incrementViralityScore(postId, 1);
        } else {
            userId = request.getAuthorId();
            userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
            // Human comment: +50 points
            redisViralityService.incrementViralityScore(postId, 50);
        }
        
        // Create comment
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentCommentId(parentCommentId);
        comment.setContent(request.getContent());
        comment.setDepthLevel(depth);
        comment.setUserAuthorId(isBot ? null : userId);
        comment.setBotAuthorId(isBot ? botId : null);
        
        Comment saved = commentRepository.save(comment);
        
        // After successful save, send notification if bot replied to a human
        if (isBot && humanTargetId != null) {
            String context = (parentCommentId == null) ? "post" : "comment";
            notificationService.notifyHumanIfNeeded(humanTargetId, botId, context);
        }
        
        return saved;
    }
}