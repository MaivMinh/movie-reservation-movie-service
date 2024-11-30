package com.foolish.movieservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "movies")
public class Movie {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  private String name;
  private String poster;
  private String backdrop;
  @NotNull
  private String description;
  private String trailer;
  @NotNull
  private Date releaseDate;
  private Double voteAverage;
  private Integer voteCount;
  private Boolean expired;

  @JsonIgnore
  @OneToMany(mappedBy = "movie", targetEntity = MovieGenre.class, cascade = CascadeType.REMOVE)
  private List<MovieGenre> movieGenres;

}
