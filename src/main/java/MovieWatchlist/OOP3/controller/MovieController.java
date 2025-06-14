package MovieWatchlist.OOP3.controller;

import MovieWatchlist.OOP3.model.Movie;
import MovieWatchlist.OOP3.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    
    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> addMovie(@RequestParam String title) {
        try {
            return ResponseEntity.ok(movieService.addMovieToWatchlist(title));
        } catch (Exception e) {
            logger.error("Error adding movie", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add movie",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Movie>> getWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(movieService.getWatchlist(PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/watched")
    public ResponseEntity<Movie> updateWatchedStatus(
            @PathVariable Long id, @RequestParam boolean watched) {
        try {
            return ResponseEntity.ok(movieService.updateWatchedStatus(id, watched));
        } catch (Exception e) {
            logger.error("Error updating watched status", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<Movie> updateRating(
            @PathVariable Long id, @RequestParam int rating) {
        try {
            return ResponseEntity.ok(movieService.updateRating(id, rating));
        } catch (Exception e) {
            logger.error("Error updating rating", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting movie", e);
            return ResponseEntity.notFound().build();
        }
    }
}