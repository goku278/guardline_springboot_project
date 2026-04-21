# Guardrail API – Backend Engineering Assignment

Overview

This project is a Spring Boot microservice designed to act as a central API gateway and guardrail system. It ensures controlled interactions between users and bots while maintaining system integrity under high concurrency.

The system uses:

* PostgreSQL as the source of truth for persistent data
* Redis for real-time guardrails, counters, and concurrency control

---

Tech Stack

* Java 17+
* Spring Boot 3.x
* Spring Data JPA
* PostgreSQL
* Redis (Spring Data Redis)
* Docker

---

Core Features

 1. REST APIs

* POST /api/posts → Create post
* POST /api/posts/{postId}/comments → Add comment
* POST /api/posts/{postId}/like → Like post

---

2. Redis Virality Engine

Each interaction updates a real-time virality score stored in Redis:

| Interaction   | Score |
| ------------- | ----- |
| Bot Reply     | +1    |
| Human Like    | +20   |
| Human Comment | +50   |

Implementation:

```java
redisTemplate.opsForValue().increment("post:{id}:virality_score", score);
```

---

Guardrail System

The system enforces strict rules using Redis atomic operations before allowing database writes.

---

Atomic Locks and Thread Safety

Problem

When multiple requests (for example, 200 bots) hit the system simultaneously, race conditions can occur:

* Multiple threads read the same value
* All proceed with updates
* Data becomes inconsistent

Example:
Two threads read count = 99 and both increment → result becomes 101 (invalid)

---

Solution: Redis Atomic Operations

Redis commands such as INCR are atomic, meaning:

* Each operation is executed completely before another begins
* No two threads can interfere with each other

---

1. Horizontal Cap (Maximum 100 Bot Replies)

Rule:
A post cannot have more than 100 bot replies.

Implementation:

```java
Long count = redisTemplate.opsForValue()
    .increment("post:" + postId + ":bot_count");

if (count > 100) {
    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
}
```

Thread Safety Guarantee:

* INCR is atomic
* Each request gets a unique incremented value
* Even with 200 concurrent requests, the system strictly stops at 100

---

2. Vertical Cap (Maximum Depth = 20)

Rule:
Comment thread depth must not exceed 20.

Implementation:

```java
if (depthLevel > 20) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
}
```

Thread Safety:

* Stateless validation
* No shared mutable state

---

3. Cooldown Cap (10 Minutes)

Rule:
A bot cannot interact with the same user more than once in 10 minutes.

Implementation:

```java
String key = "cooldown:bot:" + botId + ":user:" + userId;

if (redisTemplate.hasKey(key)) {
    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
}

redisTemplate.opsForValue()
    .set(key, "1", Duration.ofMinutes(10));
```

Thread Safety Guarantee:

* Redis handles key checks consistently
* TTL ensures automatic expiration
* No in-memory storage

---

Why This Design is Thread-Safe

1. Redis is single-threaded

   * Commands execute sequentially
   * No overlapping operations

2. Atomic operations

   * INCR ensures safe counters
   * SET with TTL ensures safe cooldowns
   * EXISTS provides consistent state checks

3. No application-level shared state

   * No use of HashMap or static variables
   * All state is stored in Redis

4. Guardrails before database writes

   * Request → Redis validation → Database write
   * Invalid requests are rejected before persistence

---

Notification Batching

To prevent notification spam:

* First interaction triggers an immediate notification
* Subsequent interactions are stored in a Redis list
* A scheduled job processes and sends a summarized notification

---

Running the Project

```bash
docker-compose up
```

Then start the Spring Boot application.

---

Testing

Use the provided Postman collection:

1. Create User
2. Create Post
3. Add Comments
4. Like Post

---

Key Takeaways

* Redis acts as a real-time control layer
* Atomic operations prevent race conditions
* The system is fully stateless
* Database integrity is maintained under concurrency

---

Conclusion

This project demonstrates how to build a high-performance, concurrency-safe backend system using Redis for control and PostgreSQL for persistence.
