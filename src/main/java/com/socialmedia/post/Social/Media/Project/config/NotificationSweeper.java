package com.socialmedia.post.Social.Media.Project.config;

import com.socialmedia.post.Social.Media.Project.service.RedisViralityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSweeper {
    private final RedisViralityService redisViralityService;
    
    @Scheduled(fixedDelay = 300000) // 5 minutes for testing
    public void sweepPendingNotifications() {
        log.info("Running notification sweeper...");
        Set<String> keys = redisViralityService.getAllUsersWithPendingNotifications();
        for (String key : keys) {
            String userIdStr = key.substring(key.lastIndexOf(':') + 1);
            Long userId = Long.valueOf(userIdStr);
            List<String> messages = redisViralityService.popAllPendingNotifications(userId);
            if (messages != null && !messages.isEmpty()) {
                // Extract first bot name and count of others
                String firstBot = extractFirstBotName(messages);
                int othersCount = messages.size() - 1;
                if (othersCount > 0) {
                    log.info("Summarized Push Notification: Bot {} and {} others interacted with your posts.", firstBot, othersCount);
                } else {
                    log.info("Summarized Push Notification: Bot {} replied to your post.", firstBot);
                }
            }
        }
    }
    
    private String extractFirstBotName(List<String> messages) {
        // messages format: "Bot X replied to your post/comment"
        if (messages.isEmpty()) return "Unknown";
        String first = messages.get(0);
        // Extract between "Bot " and " replied"
        int start = first.indexOf("Bot ") + 4;
        int end = first.indexOf(" replied", start);
        if (end > start) {
            return first.substring(start, end);
        }
        return "Unknown";
    }
}