package com.foolish.movieservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories("com.foolish.movieservice.repository")
@EntityScan("com.foolish.movieservice.model")
public class MovieServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MovieServiceApplication.class, args);
  }

}
