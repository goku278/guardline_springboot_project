package com.socialmedia.post.Social.Media.Project.service;

import com.socialmedia.post.Social.Media.Project.entity.Post;
import com.socialmedia.post.Social.Media.Project.model.dto.AuthorType;
import com.socialmedia.post.Social.Media.Project.model.dto.PostCreateRequest;
import com.socialmedia.post.Social.Media.Project.repository.BotRepository;
import com.socialmedia.post.Social.Media.Project.repository.PostRepository;
import com.socialmedia.post.Social.Media.Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    
    @Transactional
    public Post createPost(PostCreateRequest request) {
        Post post = new Post();
        post.setContent(request.getContent());

        
        if (request.getAuthorType() == AuthorType.USER) {
            userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
            post.setUserAuthorId(request.getAuthorId());
            post.setBotAuthorId(null);

            System.out.println("POST == " + post);

            log.info("POST IS => {}", post);

        } else {
            botRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bot not found"));
            post.setBotAuthorId(request.getAuthorId());
            post.setUserAuthorId(null);

            System.out.println("POST == " + post);

            log.info("POST IS => {}", post);
        }
        
        return postRepository.save(post);
    }
}