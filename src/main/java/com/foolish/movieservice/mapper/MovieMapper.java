package com.foolish.movieservice.mapper;

import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.model.MovieDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {
  MovieDTO toDTO(Movie movie);
}
