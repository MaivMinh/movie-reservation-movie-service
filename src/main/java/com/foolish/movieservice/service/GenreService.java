package com.foolish.movieservice.service;

import com.foolish.movieservice.model.Genre;
import org.springframework.stereotype.Service;

@Service
public class GenreService {
  public Genre findGenreByIdOrElseThrow(Integer id) {
    return new Genre();
  }
}
