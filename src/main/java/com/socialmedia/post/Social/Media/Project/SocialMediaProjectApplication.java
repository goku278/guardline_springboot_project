package com.socialmedia.post.Social.Media.Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocialMediaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialMediaProjectApplication.class, args);
	}

}
