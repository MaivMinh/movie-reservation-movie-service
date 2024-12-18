package com.foolish.movieservice.service;

import com.foolish.movieservice.model.Movie;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.IOException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class S3Service {
  private final S3Client s3Client;
  private final Environment env;
  private final MovieService movieService;

  private String generateUniqueFileName(String original) {
    String extension = "";
    StringBuilder fileName = new StringBuilder();
    int dotIndex = original.lastIndexOf(".");
    if (dotIndex != -1) {
      extension = original.substring(dotIndex);
      fileName.append(original, 0, dotIndex);
    }
    return fileName + "_" + UUID.randomUUID() + extension;
  }

  public String putObject(MultipartFile file) {
    String bucketName = env.getProperty("AWS_BUCKET_NAME", "us-east-1");
    String cloudFront = env.getProperty("AWS_CLOUDFRONT_DOMAIN", "https://d1bgx2mqc45r9m.cloudfront.net/");
    String key = generateUniqueFileName(file.getOriginalFilename());
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .acl("public-read")
              .build();
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
      return (cloudFront + key);
    } catch (IOException e) {
      throw new RuntimeException("Can");
    }
  }

  public void deleteObject(String key) {
    String bucketName = env.getProperty("AWS_BUCKET_NAME", "us-east-1");
    try {
      s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
    } catch (S3Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void updateObject(String key, MultipartFile file) {
    String bucketName = env.getProperty("AWS_BUCKET_NAME", "us-east-1");
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .acl("public-read")
              .build();
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
