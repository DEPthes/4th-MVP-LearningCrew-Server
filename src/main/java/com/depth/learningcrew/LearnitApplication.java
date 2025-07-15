package com.depth.learningcrew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LearnitApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnitApplication.class, args);
	}

}
