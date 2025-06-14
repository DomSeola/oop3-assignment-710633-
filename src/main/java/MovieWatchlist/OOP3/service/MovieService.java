package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
import MovieWatchlist.OOP3.model.Movie;
import MovieWatchlist.OOP3.model.MovieImage;
import MovieWatchlist.OOP3.repository.MovieImageRepository;
import MovieWatchlist.OOP3.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MovieService {
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private MovieImageRepository movieImageRepository;
    
    @Transactional
    public Movie addMovieToWatchlist(String title) throws ExecutionException, InterruptedException, IOException {
        // Fetch data from APIs in parallel
        CompletableFuture<OmdbMovie> omdbFuture = apiService.fetchOmdbMovie(title);
        CompletableFuture<TmdbMovieSearchResult> tmdbSearchFuture = apiService.searchTmdbMovie(title);
        
        CompletableFuture.allOf(omdbFuture, tmdbSearchFuture).join();
        
        OmdbMovie omdbMovie = omdbFuture.get();
        TmdbMovieSearchResult tmdbSearchResult = tmdbSearchFuture.get();
        
        if (omdbMovie == null || omdbMovie.getTitle() == null) {
            throw new RuntimeException("Movie not found in OMDb");
        }
        
        if (tmdbSearchResult == null || tmdbSearchResult.getResults() == null || tmdbSearchResult.getResults().isEmpty()) {
            throw new RuntimeException("Movie not found in TMDB");
        }
        
        // Get the first matching TMDB movie
        TmdbMovie tmdbMovie = tmdbSearchResult.getResults().get(0);
        
        // Fetch additional TMDB data in parallel
        CompletableFuture<TmdbMovieImages> imagesFuture = apiService.getTmdbMovieImages(tmdbMovie.getId());
        CompletableFuture<TmdbSimilarMovies> similarMoviesFuture = apiService.getTmdbSimilarMovies(tmdbMovie.getId());
        
        CompletableFuture.allOf(imagesFuture, similarMoviesFuture).join();
        
        TmdbMovieImages tmdbImages = imagesFuture.get();
        TmdbSimilarMovies tmdbSimilarMovies = similarMoviesFuture.get();
        
        // Create and save the movie
        Movie movie = new Movie(
            omdbMovie.getTitle(),
            Integer.parseInt(omdbMovie.getYear().replaceAll("[^0-9]", "")),
            omdbMovie.getDirector(),
            omdbMovie.getGenre()
        );
        
        // Set similar movies
        if (tmdbSimilarMovies != null && tmdbSimilarMovies.getResults() != null) {
            List<String> similarTitles = tmdbSimilarMovies.getResults().stream()
                .map(TmdbSimilarMovies.SimilarMovie::getTitle)
                .collect(Collectors.toList());
            movie.setSimilarMovies(similarTitles);
        }
        
        movie = movieRepository.save(movie);
        
        // Download and save images
        if (tmdbImages != null) {
            // Download poster if available
            if (tmdbMovie.getPosterPath() != null) {
                String posterPath = apiService.downloadImage(
                    tmdbMovie.getPosterPath(), "poster", movie.getTitle());
                MovieImage posterImage = new MovieImage(posterPath, "poster");
                posterImage.setMovie(movie);
                movieImageRepository.save(posterImage);
            }
            
            // Download backdrop if available
            if (tmdbMovie.getBackdropPath() != null) {
                String backdropPath = apiService.downloadImage(
                    tmdbMovie.getBackdropPath(), "backdrop", movie.getTitle());
                MovieImage backdropImage = new MovieImage(backdropPath, "backdrop");
                backdropImage.setMovie(movie);
                movieImageRepository.save(backdropImage);
            }
            
            // Download one more image if available from the images list
            if (tmdbImages.getBackdrops() != null && !tmdbImages.getBackdrops().isEmpty()) {
                String additionalImagePath = apiService.downloadImage(
                    tmdbImages.getBackdrops().get(0).getFilePath(), "additional", movie.getTitle());
                MovieImage additionalImage = new MovieImage(additionalImagePath, "additional");
                additionalImage.setMovie(movie);
                movieImageRepository.save(additionalImage);
            }
        }
        
        return movie;
    }
    
    public Page<Movie> getWatchlist(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }
    
    @Transactional
    public Movie updateWatchedStatus(Long movieId, boolean watched) {
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }
    
    @Transactional
    public Movie updateRating(Long movieId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setRating(rating);
        return movieRepository.save(movie);
    }
    
    @Transactional
    public void deleteMovie(Long movieId) {
        movieRepository.deleteById(movieId);
    }
}