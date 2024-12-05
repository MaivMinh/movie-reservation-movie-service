package com.foolish.movieservice.grpcClients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.lib.IdentifyRequest;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import net.devh.boot.grpc.examples.lib.IdentifyServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class IdentifyServiceGrpcClient {
  @GrpcClient("identifyService")
  private IdentifyServiceGrpc.IdentifyServiceBlockingStub identifyServiceBlockingStub;

  public IdentifyServiceGrpcClient() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
    identifyServiceBlockingStub = IdentifyServiceGrpc.newBlockingStub(channel);
  }

  public IdentifyResponse doIdentify(String token) {
    return identifyServiceBlockingStub.doIdentify(IdentifyRequest.newBuilder().setToken(token).build());
  }
}
