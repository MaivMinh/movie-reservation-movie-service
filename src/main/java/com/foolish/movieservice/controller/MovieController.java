package com.foolish.movieservice.controller;

import com.foolish.movieservice.constants.ROLE;
import com.foolish.movieservice.model.Genre;
import com.foolish.movieservice.model.Movie;
import com.foolish.movieservice.model.MovieGenre;
import com.foolish.movieservice.model.UpdatedMovie;
import com.foolish.movieservice.response.ResponseData;
import com.foolish.movieservice.response.ResponseError;
import com.foolish.movieservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
  private final IdentityService identityService;

  // Phương thức tạo ra một phim mới.
  @Transactional
  @PostMapping(value = "", consumes = {"multipart/form-data"})
  public ResponseEntity<ResponseData> createMovie(@RequestPart("movie") @Valid Movie movie, @RequestPart("poster") MultipartFile poster, @RequestPart("backdrop") MultipartFile backdrop, @RequestPart("genres") List<Integer> genreIds, HttpServletRequest request) {

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
    AuthResponse response = identityService.identityUser(request);
    if (!response.getActive() || !response.getRoles().equals(ROLE.ROLE_ADMIN)) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.UNAUTHORIZED.value(), "Forbidden!"));
    }

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
  public ResponseEntity<ResponseData> updateMovie(@RequestPart(value = "movie", required = false) UpdatedMovie movie, @RequestPart(value = "poster", required = false) MultipartFile poster, @RequestPart(value = "genres", required = false) List<Integer> genreIds, @PathVariable Integer id, @RequestPart(value = "backdrop", required = false) MultipartFile backdrop, HttpServletRequest request) {
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

    // Lấy thông tin user từ token.
    AuthResponse response = identityService.identityUser(request);
    if (!response.getActive() || !response.getRoles().equals(ROLE.ROLE_ADMIN)) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.UNAUTHORIZED.value(), "Forbidden!"));
    }

    Movie saved = movieService.findMovieByIdOrElseThrow(id);
    Map<String, String> map = new HashMap<>();


    if (movie != null) {
      if (StringUtils.hasText(movie.getName())) {
        saved.setName(movie.getName());
      }
      if (StringUtils.hasText(movie.getDescription())) {
        saved.setDescription(movie.getDescription());
      }
      if (StringUtils.hasText(movie.getTrailer())) {
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
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseData(HttpStatus.MULTI_STATUS.value(), "Partially successful", map));
    }
    return ResponseEntity.noContent().build();
  }
}
