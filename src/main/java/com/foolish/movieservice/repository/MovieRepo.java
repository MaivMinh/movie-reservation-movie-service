package com.foolish.movieservice.repository;

import com.foolish.movieservice.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepo extends JpaRepository<Movie, Integer> {
  Movie findByPoster(String poster);
  Page<Movie> findAll(Specification<Movie> specification, Pageable pageable);
  Movie findMovieById(Integer id);
}
