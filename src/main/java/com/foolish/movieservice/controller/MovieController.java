package com.foolish.movieservice.controller;

import com.foolish.movieservice.model.*;
import com.foolish.movieservice.response.ResponseData;
import com.foolish.movieservice.response.ResponseError;
import com.foolish.movieservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/movies")
public class MovieController {
  private final GenreService genreService;
  private final MovieService movieService;
  private final IdentityService identifyService;
  private final S3Service s3Service;
  private final Environment env;

  // Phương thức lấy danh sách phim.
  @GetMapping(value = "")
  public ResponseEntity<ResponseData> getMovies(@RequestParam(value = "pageSize", required = false) Integer pageSize, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "sort", required = false) String sort, HttpServletRequest request) {
    int size = (pageSize != null ? pageSize : 10);
    int pageNum = (page != null ? page : 1) - 1;
    Pageable pageable = null;

    if (StringUtils.hasText(sort))  {
      // sort=id:desc,releaseDate:asc
      List<Sort.Order> orders  = new ArrayList<>();
      String[] list = sort.split(",");
      for (String element : list) {
        orders.add(new Sort.Order(Sort.Direction.fromString(element.split(":")[1].toUpperCase()), element.split(":")[0]));
      }
      pageable = PageRequest.of(pageNum, size, Sort.by(orders));
    } else pageable = PageRequest.of(pageNum, size);
    Page<Movie> movies = movieService.findMovies(pageable);
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", movies));
  }


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

    // Kiểm tra có phải là ADMIN không.
    boolean isAdmin = identifyService.isAdmin(request);
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));
    }

    // Lưu poster và backdrop lên AWS S3.
    String url = s3Service.putObject(poster);
    if (url == null || url.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't upload poster"));
    }
    movie.setPoster(url);

    url = s3Service.putObject(backdrop);
    if (url == null || url.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can't upload backdrop"));
    }
    movie.setBackdrop(url);

    List<MovieGenre> movieGenres = genreIds.stream().map(id -> {
      MovieGenre movieGenre = new MovieGenre();
      movieGenre.setMovie(movie);
      Genre genre = genreService.findGenreByIdOrElseThrow(id);
      movieGenre.setGenre(genre);
      return movieGenre;
    }).collect(Collectors.toList());
    movie.setMovieGenres(movieGenres);
    movie.setExpired(false);
    movie.setVoteCount(0);
    movie.setVoteAverage(5.0);
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

    // Kiểm tra có phải là ADMIN không.
    boolean isAdmin = identifyService.isAdmin(request);
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));
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
      if (movie.getIsExpired() != null) {
        saved.setExpired(movie.getIsExpired());
      }
    }
    if (poster != null) {
      String url = s3Service.putObject(poster);
      if (url == null || url.isEmpty()) {
        map.put("poster", "Can't upload poster");
      } else {
        String posterUrl = saved.getPoster();
        String cloudFront = env.getProperty("AWS_CLOUDFRONT_DOMAIN", "https://d1bgx2mqc45r9m.cloudfront.net/");
        String key = posterUrl.replace(cloudFront, "");
        s3Service.deleteObject(key);
        saved.setPoster(url);
      }
    }
    if (backdrop != null) {
      String url = s3Service.putObject(backdrop);
      if (url == null || url.isEmpty()) {
        map.put("backdrop", "Can't upload backdrop");
      } else {
        String backdropUrl = saved.getBackdrop();
        String cloudFront = env.getProperty("AWS_CLOUDFRONT_DOMAIN", "https://d1bgx2mqc45r9m.cloudfront.net/");
        String key = backdropUrl.replace(cloudFront, "");
        s3Service.deleteObject(key);
        saved.setBackdrop(url);
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

  // Phương thức xóa một phim.
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<ResponseData> deleteMovie(@PathVariable String id, HttpServletRequest request) {
    // Kiểm tra có phải là ADMIN không.
    boolean isAdmin = identifyService.isAdmin(request);
    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.FORBIDDEN.value(), "Forbidden!"));
    }

    Movie movie = movieService.findMovieByIdOrElseThrow(Integer.parseInt(id));
    movieService.delete(movie);
    return ResponseEntity.ok().body(new ResponseData(HttpStatus.OK.value(), "Success", null));
  }

  // Hàm thực hiện chức năng tìm kiếm theo tiêu chí, sắp xếp và phân trang.
  @PostMapping(value = "/search")
  public ResponseEntity<ResponseData> searchMoviesByCriteria(@RequestBody @NotNull Map<String, String> criteria, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "pageNumber", required = false) Integer pageNumber, @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    /*
     Map<>: {
       name: String,
       description: String,
       release_date: +2024-10-10,
       rating: +4.5,
       vote_count: +100,
     }
     */

    int pageNum = (pageNumber != null ? pageNumber : 1) - 1;
    int size = pageSize != null ? pageSize : 10;
    Pageable pageable = null;

    if (StringUtils.hasText(sort))  {
      // sort=id:desc,releaseDate:asc
      List<Sort.Order> orders  = new ArrayList<>();
      String[] list = sort.split("");
      for (String element : list) {
        orders.add(new Sort.Order(Sort.Direction.fromString(element.split(":")[1].toUpperCase()), element.split(":")[0]));
      }
      pageable = PageRequest.of(pageNum, size, Sort.by(orders));
    } else pageable = PageRequest.of(pageNum, size);

    try {
      Page<Movie> page = movieService.findByCriteria(criteria, pageable);
      return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", page));
    } catch (ParseException e) {
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseError(HttpStatus.BAD_REQUEST.value(), "Invalid date format"));
    }
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<ResponseData> getMovieDetail(@PathVariable Integer id, HttpServletRequest request) {
    /*
    * Response:
    {
      "movie": {
          "movie_id": Integer,
          "name": String,
          "description": String,
          "trailer": String,
          "poster": String,
          "releaseDate": Date,
          * voteCount: Integer,
          * voteAverage: Integer,
      },
      "genres": [],
    */

    Movie movie = movieService.findMovieByIdOrElseThrow(id);
    List<Genre> genres = movie.getMovieGenres().stream().map(MovieGenre::getGenre).toList();
    return ResponseEntity.ok(new ResponseData(HttpStatus.OK.value(), "Success", Map.of("movie", movie, "genres", genres)));
  }
}
