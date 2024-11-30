package com.foolish.movieservice.repository;

import com.foolish.movieservice.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepo extends JpaRepository<Genre, Integer> {
}
