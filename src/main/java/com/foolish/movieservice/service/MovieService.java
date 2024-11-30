package com.foolish.movieservice.service;

import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.repository.MovieRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MovieService {
  private final MovieRepo movieRepo;

  public Movie findMovieByPoster(String combine) {
    return new Movie();
  }

  public Movie save(Movie movie) {
    return new Movie();
  }

  public Movie findMovieByIdOrElseThrow(Integer id) {
    return null;
  }

  public List<Movie> findAll() {
    return movieRepo.findAll();
  }
}
