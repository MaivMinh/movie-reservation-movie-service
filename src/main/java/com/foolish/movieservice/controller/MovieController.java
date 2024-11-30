package com.foolish.movieservice.controller;

import com.foolish.movieservice.model.Genre;
import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.model.MovieGenre;
import com.foolish.movieservice.model.UpdatedMovie;
import com.foolish.movieservice.response.ResponseData;
import com.foolish.movieservice.response.ResponseError;
import com.foolish.movieservice.service.AuthGrpcClient;
import com.foolish.movieservice.service.AzureBlobService;
import com.foolish.movieservice.service.GenreService;
import com.foolish.movieservice.service.MovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/movies")
public class MovieController {
  private final AzureBlobService azureBlobService;
  private final GenreService genreService;
  private final MovieService movieService;
  private final AuthGrpcClient grpcClient;

  // Phương thức tạo ra một phim mới.
  @Transactional
  @PostMapping(value = "", consumes = {"multipart/form-data"})
  public ResponseEntity<ResponseData> createMovie(@RequestPart("movie") @Valid Movie movie, @RequestPart("poster") MultipartFile poster, @RequestPart("backdrop") MultipartFile backdrop, @RequestPart("genres") List<Integer> genreIds) {

    /*
    * {
      "movie": {
          "name": String,
          "description": String,
          "trailer": String,
          "releaseDate": Date
      },
      "poster": MultipartFile,
      "backdrop": MultipartFile,
      "genres": []
      }
    * */

    // Lưu poster lên Azure blob. Sau đó lưu Movie mới vào hệ thống.
    String url = azureBlobService.writeBlobFile(poster);
    if (url == null || url.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't upload poster"));
    }
    movie.setPoster(url);

    // Lưu backdrop lên Azure blob.
    String backdropUrl = azureBlobService.writeBlobFile(backdrop);
    if (backdropUrl == null || backdropUrl.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't upload backdrop"));
    }
    movie.setBackdrop(backdropUrl);

    List<MovieGenre> movieGenres = genreIds.stream().map(id -> {
      MovieGenre movieGenre = new MovieGenre();
      movieGenre.setMovie(movie);
      Genre genre = genreService.findGenreByIdOrElseThrow(id);
      movieGenre.setGenre(genre);
      return movieGenre;
    }).collect(Collectors.toList());
    movie.setMovieGenres(movieGenres);
    Movie saved = movieService.save(movie);
    if (saved == null || saved.getId() <= 0) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't save movie"));
    }
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", saved));
  }


  // Phương thức update movie.
  @Transactional
  @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
  public ResponseEntity<ResponseData> updateMovie(@RequestPart(value = "movie", required = false)UpdatedMovie movie, @RequestPart(value = "poster", required = false) MultipartFile poster, @RequestPart(value = "genres", required = false) List<Integer> genreIds, @PathVariable Integer id, @RequestPart(value = "backdrop", required = false) MultipartFile backdrop) {
    /* Dữ liệu update tuỳ thuộc vào client.
    * "movie": {
          "name": String,
          "description": String,
          "trailer": String,
          "releaseDate": Date,
      },
      "poster": MultipartFile,
      "backdrop": MultipartFile,
      "genres": []
    *
    * */

    Movie saved = movieService.findMovieByIdOrElseThrow(id);
    Map<String, String> map = new HashMap<>();


    if (movie != null) {
      if (movie.getName() != null && !movie.getName().isEmpty()) {
        saved.setName(movie.getName());
      }
      if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
        saved.setDescription(movie.getDescription());
      }
      if (movie.getTrailer() != null && !movie.getTrailer().isEmpty()) {
        saved.setTrailer(movie.getTrailer());
      }
      if (movie.getReleaseDate() != null) {
        saved.setReleaseDate(movie.getReleaseDate());
      }
    }
    if (poster != null) {
      String url = azureBlobService.writeBlobFile(poster);
      if (url == null || url.isEmpty()) {
        map.put("poster", "Can't upload poster");
      } else {
        azureBlobService.deleteBlobFile(saved.getPoster());
        saved.setPoster(url);
      }
    }
    if (backdrop != null) {
      String url = azureBlobService.writeBlobFile(backdrop);
      if (url == null || url.isEmpty()) {
        map.put("backdrop", "Can't upload backdrop");
      } else {
        azureBlobService.deleteBlobFile(saved.getBackdrop());
        saved.setPoster(url);
      }
    }
    if (genreIds != null) {
      List<MovieGenre> movieGenres = genreIds.stream().map(genreId -> {
        Genre genre = genreService.findGenreByIdOrElseThrow(genreId);
        MovieGenre movieGenre = new MovieGenre();
        movieGenre.setGenre(genre);
        movieGenre.setMovie(saved);
        return movieGenre;
      }).collect(Collectors.toList());
      saved.setMovieGenres(movieGenres);
    }
    movieService.save(saved); // Cập nhật lại phim vào DB.

    if (!map.isEmpty()) {
      return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ResponseData(HttpStatus.OK.value(), "Partially successful", map));
    }
    return ResponseEntity.noContent().build();
  }

  // Lấy danh sách phim.
  @GetMapping("")
  public ResponseEntity<ResponseData> getMovies() {
    AuthResponse response = grpcClient.doIntrospect("eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJBdXRob3JpemF0aW9uIHNlcnZpY2UiLCJzdWIiOiJBY2Nlc3MgVG9rZW4iLCJ1c2VybmFtZSI6ImFkbWluIiwicm9sZXMiOiJST0xFX0FETUlOIiwiaWF0IjoxNzMyODA5NjA0LCJleHAiOjE3MzM0MTQ0MDR9.89aWX06-JZXCBFUXU_AtHCgp4VyGY0r9WSNxp5SElfTYFp7rR4PjFQCi5sAIGk__");
    if (response.getActive()) return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", Map.of("active", true, "username", response.getUsername(), "roles", response.getRoles())));
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
  }

}
