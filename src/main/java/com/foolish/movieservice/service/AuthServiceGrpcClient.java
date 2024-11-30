package com.foolish.movieservice.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.lib.AuthRequest;
import net.devh.boot.grpc.examples.lib.AuthResponse;
import net.devh.boot.grpc.examples.lib.AuthServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceGrpcClient {

  @GrpcClient("authService")
  private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

  public AuthServiceGrpcClient() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
    authServiceBlockingStub = AuthServiceGrpc.newBlockingStub(channel);
  }

  public AuthResponse doIntrospect(String token) {
    AuthRequest request = AuthRequest.newBuilder().setToken(token).build();
    return authServiceBlockingStub.doIntrospect(request);
  }
}
