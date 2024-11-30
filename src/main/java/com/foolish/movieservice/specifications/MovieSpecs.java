package com.foolish.movieservice.specifications;

import com.foolish.movieservice.model.Genre;
import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.model.MovieGenre;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class MovieSpecs {
  public static Specification<Movie> containsName(String name) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.like(root.get("name"), "%" + name + "%");
      }
    };
  }

  public static Specification<Movie> containsGenre(Integer genreId) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final Path<MovieGenre> movieGenres = root.get("movieGenres");
        final Path<Genre> genre = movieGenres.get("genre");
        return cb.equal(genre.get("id"), genreId);
      }
    };
  }

  public static Specification<Movie> containsDescription(String desc) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.like(root.get("description"), "%" + desc + "%");
      }
    };
  }


  public static Specification<Movie> releaseDateAfter(Date date) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.greaterThan(root.get("releaseDate"), date);
      }
    };
  }

  public static Specification<Movie> releaseDateEqualTo(Date date) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("releaseDate"), date);
      }
    };
  }

  public static Specification<Movie> releaseDateBefore(Date date) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.lessThan(root.get("releaseDate"), date);
      }
    };
  }

  public static Specification<Movie> voteAverageGreaterThan(double given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.greaterThan(root.get("voteAverage"), given);
      }
    };
  }

  public static Specification<Movie> voteAverageLessThan(double given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.lessThan(root.get("voteAverage"), given);
      }
    };
  }

  public static Specification<Movie> voteAverageEqualTo(double given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("voteAverage"), given);
      }
    };
  }

  public static Specification<Movie> voteCountGreaterThan(int given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.greaterThanOrEqualTo(root.get("voteCount"), given);
      }
    };
  }

  public static Specification<Movie> voteCountLessThan(int given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.lessThan(root.get("voteCount"), given);
      }
    };
  }

  public static Specification<Movie> voteCountEqualTo(int given) {
    return new Specification<Movie>() {
      @Override
      public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("voteCount"), given);
      }
    };
  }
}
