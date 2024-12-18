package com.foolish.movieservice.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
  // S3 configuration
  private final AwsCredentials credentials;
  private final Environment env;

  public S3Config(Environment env) {
    this.env = env;
    String accessKey = env.getProperty("AWS_ACCESS_KEY");
    String secretKey = env.getProperty("AWS_SECRET_KEY");
    credentials = AwsBasicCredentials.create(accessKey, secretKey);
  }

  @Bean
  public S3Client s3Client() {
    String region = env.getProperty("AWS_REGION", "us-east-1");
    return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
  }
}
