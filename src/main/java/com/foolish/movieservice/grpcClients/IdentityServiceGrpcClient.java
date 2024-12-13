package com.foolish.movieservice.grpcClients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.lib.IdentityRequest;
import net.devh.boot.grpc.examples.lib.IdentityResponse;
import net.devh.boot.grpc.examples.lib.IdentityServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class IdentityServiceGrpcClient {
  @GrpcClient("identityService")
  private final IdentityServiceGrpc.IdentityServiceBlockingStub identityServiceBlockingStub;

  @Autowired
  public IdentityServiceGrpcClient(Environment env) {
    String name = env.getProperty("AUTH_GRPC_SERVER_NAME", "localhost");
    int port = Integer.parseInt(Objects.requireNonNull(env.getProperty("AUTH_GRPC_SERVER_PORT", "9091")));
    System.out.println("gRPC auth server: " + name + ":" + port);
    ManagedChannel channel = ManagedChannelBuilder.forAddress(name, port).usePlaintext().build();
    identityServiceBlockingStub = IdentityServiceGrpc.newBlockingStub(channel);
  }

  public IdentityResponse doIdentity(String userId) {
    IdentityRequest request = IdentityRequest.newBuilder().setUserId(userId).build();
    try {
      return identityServiceBlockingStub.doIdentity(request);
    } catch (RuntimeException e) {
      log.error("RPC failed: {}", String.valueOf(e.getCause()));
      return null;
    }
  }
}
