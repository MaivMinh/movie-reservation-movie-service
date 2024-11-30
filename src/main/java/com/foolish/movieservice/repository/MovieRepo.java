package com.foolish.movieservice.repository;

import com.foolish.movieservice.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepo extends JpaRepository<Movie, Integer> {
}
