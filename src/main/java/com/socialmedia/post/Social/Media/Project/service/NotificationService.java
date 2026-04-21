package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.Bot;
import com.socialmedia.post.Social.Media.Project.repository.BotRepository;
import com.socialmedia.post.Social.Media.Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final RedisViralityService redisViralityService;
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    
    public void notifyHumanIfNeeded(Long humanId, Long botId, String contextMessage) {
        if (humanId == null) return;
        
        // Check if user exists
        userRepository.findById(humanId).orElse(null);
        
        String botName = botRepository.findById(botId).map(Bot::getName).orElse("Unknown Bot");
        String notificationMsg = String.format("Bot %s replied to your %s", botName, contextMessage);
        
        boolean canSend = redisViralityService.canSendNotification(humanId);
        if (canSend) {
            log.info("Push Notification Sent to User {}: {}", humanId, notificationMsg);
        } else {
            redisViralityService.pushPendingNotification(humanId, notificationMsg);
            log.debug("Queued pending notification for user {}: {}", humanId, notificationMsg);
        }
    }
}