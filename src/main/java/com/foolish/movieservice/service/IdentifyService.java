package com.foolish.movieservice.service;

import com.foolish.movieservice.grpcClients.IdentifyServiceGrpcClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdentifyService {
  private final IdentifyServiceGrpcClient identifyServiceGrpcClient;

  public IdentifyResponse doIdentify(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    String accessToken = token.substring(7);
    return identifyServiceGrpcClient.doIdentify(accessToken);
  }
}
