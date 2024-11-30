package com.foolish.movieservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "genres")
public class Genre {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String name;

  @JsonIgnore
  @OneToMany(mappedBy = "genre", targetEntity = MovieGenre.class, cascade = CascadeType.REMOVE)
  private Set<MovieGenre> movieGenres;
}
