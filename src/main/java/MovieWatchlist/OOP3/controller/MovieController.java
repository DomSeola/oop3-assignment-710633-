package MovieWatchlist.OOP3.controller;

import MovieWatchlist.OOP3.model.Movie;
import MovieWatchlist.OOP3.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;
    
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestParam String title) {
        try {
            Movie movie = movieService.addMovieToWatchlist(title);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<Movie>> getWatchlist(Pageable pageable) {
        return ResponseEntity.ok(movieService.getWatchlist(pageable));
    }
    
    @PatchMapping("/{id}/watched")
    public ResponseEntity<Movie> updateWatchedStatus(
            @PathVariable Long id, @RequestParam boolean watched) {
        try {
            return ResponseEntity.ok(movieService.updateWatchedStatus(id, watched));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/rating")
    public ResponseEntity<Movie> updateRating(
            @PathVariable Long id, @RequestParam int rating) {
        try {
            return ResponseEntity.ok(movieService.updateRating(id, rating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}