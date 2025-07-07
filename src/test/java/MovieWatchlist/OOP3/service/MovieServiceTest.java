package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
import MovieWatchlist.OOP3.exception.*;
import MovieWatchlist.OOP3.model.Movie;
import MovieWatchlist.OOP3.model.MovieImage;
import MovieWatchlist.OOP3.repository.MovieImageRepository;
import MovieWatchlist.OOP3.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {
 @Mock
 private OmdbClient omdbClient;
 @Mock
 private TmdbClient tmdbClient;
 @Mock
 private MovieRepository movieRepository;
 @Mock
 private MovieImageRepository movieImageRepository;
 @Mock
 private ApiService apiService;
 
 @InjectMocks
 private MovieService movieService;

 @Test
 void updateRating_shouldThrowWhenRatingInvalid() {
     assertThrows(InvalidRatingException.class, 
         () -> movieService.updateRating(1L, 6));
 }

 @Test
 void updateRating_shouldThrowWhenMovieNotFound() {
     when(movieRepository.findById(1L)).thenReturn(Optional.empty());
     assertThrows(MovieNotFoundException.class, 
         () -> movieService.updateRating(1L, 3));
 }

 @Test
 void updateRating_shouldUpdateWhenValid() {
     Movie movie = new Movie("Test", 2023, "Director", "Genre");
     when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
     when(movieRepository.save(any())).thenReturn(movie);
     
     Movie result = movieService.updateRating(1L, 4);
     assertEquals(4, result.getRating());
 }

 @Test
 void getWatchlist_shouldReturnPaginatedResults() {
     PageRequest pageable = PageRequest.of(0, 10);
     when(movieRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Movie())));
     
     Page<Movie> result = movieService.getWatchlist(pageable);
     assertEquals(1, result.getTotalElements());
 }

 @Test
 void deleteMovie_shouldThrowWhenMovieNotFound() {
     when(movieRepository.existsById(1L)).thenReturn(false);
     assertThrows(MovieNotFoundException.class, 
         () -> movieService.deleteMovie(1L));
 }
}