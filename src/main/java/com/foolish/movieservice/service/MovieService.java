package com.foolish.movieservice.service;

import com.foolish.movieservice.exceptions.ResourceNotFoundException;
import com.foolish.movieservice.mapper.MovieMapper;
import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.model.MovieDTO;
import com.foolish.movieservice.repository.MovieRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

import static com.foolish.movieservice.specifications.MovieSpecs.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
@AllArgsConstructor
public class MovieService {
  private final MovieRepo movieRepo;
  private final MovieMapper movieMapper;

  public Movie findMovieByPoster(String combine) {
    return movieRepo.findByPoster(combine);
  }

  public Movie save(Movie movie) {
    return movieRepo.save(movie);
  }

  public Movie findMovieByIdOrElseThrow(Integer id) {
    return movieRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found", Map.of("id", String.valueOf(id))));
  }

  public Page<MovieDTO> findMovieDTOs(Pageable pageable) {
    Page<Movie> pages = movieRepo.findAll(pageable);
    return new PageImpl<>(pages.getContent().stream().map(movieMapper::toDTO).toList(), pageable, pages.getTotalElements());
  }

  public Page<Movie> findMovies(Pageable pageable) {
    return movieRepo.findAll(pageable);
  }

  public Page<Movie> findByCriteria(Map<String, String> criteria, Pageable pageable) {
    /*
     * Map<>: {
     * name: String,
     * description: String,
     * release_date: +2024-10-10,
     * rating: +4.5,
     * vote_count: +100,
     * }
     *
     * */

    Specification<Movie> specification = where(null);
    if (StringUtils.hasText(criteria.get("name"))) {
      specification = specification.and(containsName(criteria.get("name")));
    }
    if (StringUtils.hasText(criteria.get("description"))) {
      specification = specification.and(containsDescription(criteria.get("description")));
    }
    if (StringUtils.hasText(criteria.get("release_date"))) {
      String value = String.valueOf(criteria.get("release_date"));
      char c = value.charAt(0);
      if (c == '+') {
        String d = value.substring(1);
        Date date = new Date(Long.parseLong(d));
        specification = specification.and(where(releaseDateAfter(date).or(releaseDateEqualTo(date))));
      } else if (c == '-') {
        String d = value.substring(1);
        Date date = new Date(Long.parseLong(d));
        specification = specification.and(where(releaseDateBefore(date).or(releaseDateEqualTo(date))));
      } else {
        Date date = new Date(Long.parseLong(value));
        specification = specification.and(releaseDateEqualTo(date));
      }
    }

    if (StringUtils.hasText(criteria.get("rating"))) {
      String value = String.valueOf(criteria.get("rating"));
      char c = value.charAt(0);
      if (c == '+') {
        double given = Double.parseDouble(value.substring(1));
        specification = specification.and(where(voteAverageGreaterThan(given).or(voteAverageEqualTo(given))));
      } else if (c == '-') {
        double given = Double.parseDouble(value.substring(1));
        specification = specification.and(where(voteAverageLessThan(given).or(voteAverageEqualTo(given))));
      } else {
        double given = Double.parseDouble(value);
        specification = specification.and(where(voteAverageEqualTo(given)));
      }
    }
    if (StringUtils.hasText(criteria.get("vote_count"))) {
      String value = String.valueOf(criteria.get("vote_count"));
      char c = value.charAt(0);
      if (c == '+') {
        int given = Integer.parseInt(value.substring(1));
        specification = specification.and(where(voteCountGreaterThan(given).or(voteCountEqualTo(given))));
      } else if (c == '-') {
        int given = Integer.parseInt(value.substring(1));
        specification = specification.and(where(voteCountLessThan(given).or(voteCountEqualTo(given))));
      } else {
        int given = Integer.parseInt(value);
        specification = specification.and(where(voteCountEqualTo(given)));
      }
    }
    return movieRepo.findAll(specification, pageable);
  }

  public Movie findMovieById(Integer id) {
    return movieRepo.findMovieById(id);
  }

  public void delete(Movie movie) {
    movieRepo.delete(movie);
  }
}
