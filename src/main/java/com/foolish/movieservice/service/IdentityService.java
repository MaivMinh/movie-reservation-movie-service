package com.foolish.movieservice.service;

import jakarta.servlet.http.HttpServletRequest;
import net.devh.boot.grpc.examples.lib.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public class IdentityService {
  private final AuthServiceGrpcClient authServiceGrpcClient;

  public IdentityService(AuthServiceGrpcClient authServiceGrpcClient) {
    this.authServiceGrpcClient = authServiceGrpcClient;
  }

  public AuthResponse identityUser(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    String accessToken = token.substring(7);
    System.out.println(accessToken);
    return authServiceGrpcClient.doIntrospect(accessToken);
  }

}
