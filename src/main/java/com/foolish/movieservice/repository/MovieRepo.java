package com.foolish.movieservice.repository;

import com.foolish.movieservice.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepo extends JpaRepository<Movie, Integer> {
  Movie findByPoster(String poster);
}
