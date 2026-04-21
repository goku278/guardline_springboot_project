package com.socialmedia.post.Social.Media.Project.controller;

import com.socialmedia.post.Social.Media.Project.entity.Bot;
import com.socialmedia.post.Social.Media.Project.entity.User;
import com.socialmedia.post.Social.Media.Project.service.BotService;
import com.socialmedia.post.Social.Media.Project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bot createUser(@RequestBody Bot bot) {
        return botService.createBot(bot);
    }
}