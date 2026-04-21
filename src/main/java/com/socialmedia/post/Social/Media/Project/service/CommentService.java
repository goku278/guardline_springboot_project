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

        Integer depth;
        Long parentCommentId = request.getParentCommentId();
        Long humanTargetId = null;
        
        if (parentCommentId == null) {
            depth = 1;
            if (post.getUserAuthorId() != null) {
                humanTargetId = post.getUserAuthorId();
            }
        } else {
            Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));
            depth = parent.getDepthLevel() + 1;
            if (parent.getUserAuthorId() != null) {
                humanTargetId = parent.getUserAuthorId();
            }
        }

        Long botId = null;
        Long userId = null;
        boolean isBot = request.getAuthorType() == AuthorType.BOT;
        
        if (isBot) {
            botId = request.getAuthorId();
            botRepository.findById(botId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bot not found"));

            guardrailService.checkHorizontalCapAtomic(postId);

            guardrailService.checkVerticalCap(parentCommentId);

            Long cooldownHumanId = (post.getUserAuthorId() != null) ? post.getUserAuthorId() : 
                                    (parentCommentId != null ? 
                                     commentRepository.findById(parentCommentId).map(c -> c.getUserAuthorId()).orElse(null) : null);
            if (cooldownHumanId != null) {
                guardrailService.checkBotHumanCooldown(botId, cooldownHumanId);
            }

            redisViralityService.incrementViralityScore(postId, 1);
        } else {
            userId = request.getAuthorId();
            userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
            redisViralityService.incrementViralityScore(postId, 50);
        }
        
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentCommentId(parentCommentId);
        comment.setContent(request.getContent());
        comment.setDepthLevel(depth);
        comment.setUserAuthorId(isBot ? null : userId);
        comment.setBotAuthorId(isBot ? botId : null);
        
        Comment saved = commentRepository.save(comment);

        if (isBot && humanTargetId != null) {
            String context = (parentCommentId == null) ? "post" : "comment";
            notificationService.notifyHumanIfNeeded(humanTargetId, botId, context);
        }
        
        return saved;
    }
}