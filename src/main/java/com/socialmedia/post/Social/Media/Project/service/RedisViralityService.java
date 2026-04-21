package com.socialmedia.post.Social.Media.Project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisViralityService {
    public final RedisTemplate<String, String> redisTemplate;  // Make public for GuardrailService if needed, but better to keep private and expose method

    private static final String VIRALITY_KEY_PREFIX = "post:virality:";
    private static final String BOT_COUNT_KEY_PREFIX = "post:bot_count:";
    private static final String COOLDOWN_KEY_PREFIX = "cooldown:bot:human:";
    private static final String NOTIF_COOLDOWN_PREFIX = "notif:cooldown:user:";
    private static final String PENDING_NOTIF_PREFIX = "user:pending_notifs:";

    public void incrementViralityScore(Long postId, int points) {
        String key = VIRALITY_KEY_PREFIX + postId;
        redisTemplate.opsForValue().increment(key, points);
    }

    public Long incrementBotCount(Long postId) {
        String key = BOT_COUNT_KEY_PREFIX + postId;
        return redisTemplate.opsForValue().increment(key);
    }


    public boolean tryIncrementBotCount(Long postId, int maxAllowed) {
        String key = BOT_COUNT_KEY_PREFIX + postId;
        String luaScript =
                "local current = redis.call('GET', KEYS[1]) " +
                        "if current == false then current = 0 end " +
                        "if tonumber(current) < tonumber(ARGV[1]) then " +
                        "   redis.call('INCR', KEYS[1]) " +
                        "   return 1 " +
                        "else " +
                        "   return 0 " +
                        "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, List.of(key), String.valueOf(maxAllowed));
        return result != null && result == 1L;
    }

    public boolean checkAndSetCooldown(Long botId, Long humanId, long ttlSeconds) {
        String key = COOLDOWN_KEY_PREFIX + botId + ":" + humanId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(success);
    }

    public boolean isCooldownPresent(Long botId, Long humanId) {
        String key = COOLDOWN_KEY_PREFIX + botId + ":" + humanId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean canSendNotification(Long userId) {
        String key = NOTIF_COOLDOWN_PREFIX + userId;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(15));
        return true;
    }

    public void pushPendingNotification(Long userId, String message) {
        String key = PENDING_NOTIF_PREFIX + userId;
        redisTemplate.opsForList().rightPush(key, message);
    }

    public List<String> popAllPendingNotifications(Long userId) {
        String key = PENDING_NOTIF_PREFIX + userId;
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
        if (messages != null && !messages.isEmpty()) {
            redisTemplate.delete(key);
        }
        return messages != null ? messages : List.of();
    }

    public Set<String> getAllUsersWithPendingNotifications() {
        String pattern = PENDING_NOTIF_PREFIX + "*";
        return redisTemplate.keys(pattern);
    }
}