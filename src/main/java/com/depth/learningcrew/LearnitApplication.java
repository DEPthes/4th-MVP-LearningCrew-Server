package com.depth.learningcrew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class LearnitApplication {

  public static void main(String[] args) {
    loadEnvironment();

    SpringApplication.run(LearnitApplication.class, args);
  }

  private static void loadEnvironment() {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
  }

}
