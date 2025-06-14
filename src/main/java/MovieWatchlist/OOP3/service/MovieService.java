package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
import MovieWatchlist.OOP3.model.Movie;
import MovieWatchlist.OOP3.model.MovieImage;
import MovieWatchlist.OOP3.repository.MovieImageRepository;
import MovieWatchlist.OOP3.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    
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
        
        if (omdbMovie.getTitle() == null || tmdbSearchResult.getResults().isEmpty()) {
            throw new RuntimeException("Movie not found in one or both APIs");
        }
        
        TmdbMovie tmdbMovie = tmdbSearchResult.getResults().get(0);
        
        // Fetch additional data
        CompletableFuture<TmdbMovieImages> imagesFuture = apiService.getTmdbMovieImages(tmdbMovie.getId());
        CompletableFuture<TmdbSimilarMovies> similarMoviesFuture = apiService.getTmdbSimilarMovies(tmdbMovie.getId());
        CompletableFuture.allOf(imagesFuture, similarMoviesFuture).join();
        
        // Create and save movie
        Movie movie = new Movie(
            omdbMovie.getTitle(),
            Integer.parseInt(omdbMovie.getYear().replaceAll("\\D", "")),
            omdbMovie.getDirector(),
            omdbMovie.getGenre()
        );
        
        // Set similar movies
        movie.setSimilarMovies(
            similarMoviesFuture.get().getResults().stream()
                .map(TmdbSimilarMovies.SimilarMovie::getTitle)
                .collect(Collectors.toList())
        );
        
        movie = movieRepository.save(movie);
        
        // Download and save images
        processImages(movie, imagesFuture.get(), tmdbMovie);
        
        return movie;
    }

    private void processImages(Movie movie, TmdbMovieImages images, TmdbMovie tmdbMovie) throws IOException {
        // Download poster if available
        if (tmdbMovie.getPosterPath() != null) {
            String posterPath = apiService.downloadImage(tmdbMovie.getPosterPath(), "poster", movie.getTitle());
            MovieImage posterImage = new MovieImage(posterPath, "poster");
            posterImage.setMovie(movie);
            movieImageRepository.save(posterImage);
        }
        
        // Download backdrop if available
        if (tmdbMovie.getBackdropPath() != null) {
            String backdropPath = apiService.downloadImage(tmdbMovie.getBackdropPath(), "backdrop", movie.getTitle());
            MovieImage backdropImage = new MovieImage(backdropPath, "backdrop");
            backdropImage.setMovie(movie);
            movieImageRepository.save(backdropImage);
        }
        
        // Download one more image if available
        if (images.getBackdrops() != null && !images.getBackdrops().isEmpty()) {
            String additionalPath = apiService.downloadImage(
                images.getBackdrops().get(0).getFilePath(), "additional", movie.getTitle());
            MovieImage additionalImage = new MovieImage(additionalPath, "additional");
            additionalImage.setMovie(movie);
            movieImageRepository.save(additionalImage);
        }
    }

    @Transactional(readOnly = true)
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