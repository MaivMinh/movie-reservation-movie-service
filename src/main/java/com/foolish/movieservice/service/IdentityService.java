package com.foolish.movieservice.service;

import com.foolish.movieservice.constants.HEADER;
import com.foolish.movieservice.constants.ROLE;
import com.foolish.movieservice.grpcClients.IdentityServiceGrpcClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.IdentityResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdentityService {
  private final IdentityServiceGrpcClient identityServiceGrpcClient;

  public IdentityResponse doIdentify(String userId) {
    return identityServiceGrpcClient.doIdentity(userId);
  }

  public boolean isAdmin(HttpServletRequest request) {
    String userId = request.getHeader(HEADER.X_USER_ID);
    if (userId == null || userId.isEmpty()) {
      return false;
    }
    IdentityResponse response = doIdentify(userId);
    return response.getActive() && response.getRoles().equals(ROLE.ADMIN);
  }
}
