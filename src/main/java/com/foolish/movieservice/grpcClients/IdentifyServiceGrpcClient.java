package com.foolish.movieservice.grpcClients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.examples.lib.IdentifyRequest;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import net.devh.boot.grpc.examples.lib.IdentifyServiceGrpc;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class IdentifyServiceGrpcClient extends IdentifyServiceGrpc.IdentifyServiceImplBase {
  private final IdentifyServiceGrpc.IdentifyServiceBlockingStub identifyServiceBlockingStub;

  public IdentifyServiceGrpcClient(Environment env) {
    String name = env.getProperty("AUTH_GRPC_SERVER_NAME");
    int port = Integer.parseInt(Objects.requireNonNull(env.getProperty("AUTH_GRPC_SERVER_PORT")));
    ManagedChannel channel = ManagedChannelBuilder.forAddress(name, port).usePlaintext().build();
    this.identifyServiceBlockingStub = IdentifyServiceGrpc.newBlockingStub(channel);
  }

  public IdentifyResponse doIdentify(String token) {
    IdentifyRequest request = IdentifyRequest.newBuilder().setToken(token).build();
    return identifyServiceBlockingStub.doIdentify(request);
  }
}
