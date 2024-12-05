package com.foolish.movieservice.grpcServers;

import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.service.MovieService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.CheckActiveStateRequest;
import net.devh.boot.grpc.examples.lib.CheckActiveStateResponse;
import net.devh.boot.grpc.examples.lib.CheckActiveStateServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;


@GrpcService
@Service
@AllArgsConstructor
public class CheckActiveServiceServer extends CheckActiveStateServiceGrpc.CheckActiveStateServiceImplBase {
  private final MovieService movieService;

  @Override
  public void doCheckActiveState(CheckActiveStateRequest request, StreamObserver<CheckActiveStateResponse> responseObserver) {
    // Thực hiện tìm kiếm movie và trạng thái isExpired của nó.
    int id = request.getId();

    Movie movie = movieService.findMovieById(id);
    if (movie == null || movie.getId() <= 0 || movie.getExpired()) {
      responseObserver.onNext(CheckActiveStateResponse.newBuilder().setActive(false).build());
    } else {
      responseObserver.onNext(CheckActiveStateResponse.newBuilder().setActive(true).build());
    }
    responseObserver.onCompleted();
  }
}
