package com.foolish.movieservice.grpcServers;

import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.service.MovieService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.ActiveStateRequest;
import net.devh.boot.grpc.examples.lib.ActiveStateResponse;
import net.devh.boot.grpc.examples.lib.CheckMovieActiveStateServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;


@GrpcService
@Service
@AllArgsConstructor
public class CheckActiveServiceServer extends CheckMovieActiveStateServiceGrpc.CheckMovieActiveStateServiceImplBase {
  private final MovieService movieService;

  @Override
  public void doCheckMovieActiveState(ActiveStateRequest request, StreamObserver<ActiveStateResponse> responseObserver) {
    // Thực hiện tìm kiếm movie và trạng thái isExpired của nó.
    int id = request.getId();

    Movie movie = movieService.findMovieById(id);
    if (movie == null || movie.getId() <= 0 || movie.getExpired()) {
      responseObserver.onNext(ActiveStateResponse.newBuilder().setActive(false).build());
    } else {
      responseObserver.onNext(ActiveStateResponse.newBuilder().setActive(true).build());
    }
    responseObserver.onCompleted();
  }
}
