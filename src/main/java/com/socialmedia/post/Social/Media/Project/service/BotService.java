package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.Bot;
import com.socialmedia.post.Social.Media.Project.entity.User;
import com.socialmedia.post.Social.Media.Project.repository.BotRepository;
import com.socialmedia.post.Social.Media.Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {

    private final BotRepository botRepository;

    public Bot createBot(Bot bot) {

        botRepository.findByName(bot.getName())
                .ifPresent(u -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Username already exists"
                    );
                });

        return botRepository.save(bot);
    }
}
