package com.foolish.movieservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "movie_genres")
public class MovieGenre {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(targetEntity = Movie.class)
  @JoinColumn(name = "movie_id")
  private Movie movie;

  @ManyToOne(targetEntity = Genre.class)
  @JoinColumn(name = "genre_id")
  private Genre genre;
}
