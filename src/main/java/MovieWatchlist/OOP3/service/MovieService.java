package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
import MovieWatchlist.OOP3.exception.*;
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
    
    private final OmdbClient omdbClient;
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;
    private final ApiService apiService;

    @Autowired
    public MovieService(OmdbClient omdbClient, TmdbClient tmdbClient,
                      MovieRepository movieRepository, MovieImageRepository movieImageRepository,
                      ApiService apiService) {
        this.omdbClient = omdbClient;
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
        this.movieImageRepository = movieImageRepository;
        this.apiService = apiService;
    }

    @Transactional
    public Movie addMovieToWatchlist(String title) throws ExecutionException, InterruptedException, IOException {
        CompletableFuture<OmdbMovie> omdbFuture = CompletableFuture.supplyAsync(() -> omdbClient.fetchMovie(title));
        CompletableFuture<TmdbMovieSearchResult> tmdbSearchFuture = CompletableFuture.supplyAsync(() -> tmdbClient.searchMovie(title));
        
        CompletableFuture.allOf(omdbFuture, tmdbSearchFuture).join();
        
        OmdbMovie omdbMovie = omdbFuture.get();
        TmdbMovieSearchResult tmdbSearchResult = tmdbSearchFuture.get();
        
        if (omdbMovie.getTitle() == null || tmdbSearchResult.getResults().isEmpty()) {
            throw new MovieNotFoundException("Movie not found in one or both APIs");
        }
        
        TmdbMovie tmdbMovie = tmdbSearchResult.getResults().get(0);
        
        CompletableFuture<TmdbMovieImages> imagesFuture = CompletableFuture.supplyAsync(() -> tmdbClient.getMovieImages(tmdbMovie.getId()));
        CompletableFuture<TmdbSimilarMovies> similarMoviesFuture = CompletableFuture.supplyAsync(() -> tmdbClient.getSimilarMovies(tmdbMovie.getId()));
        CompletableFuture.allOf(imagesFuture, similarMoviesFuture).join();
        
        Movie movie = new Movie(
            omdbMovie.getTitle(),
            Integer.parseInt(omdbMovie.getYear().replaceAll("\\D", "")),
            omdbMovie.getDirector(),
            omdbMovie.getGenre()
        );
        
        movie.setSimilarMovies(
            similarMoviesFuture.get().getResults().stream()
                .map(TmdbSimilarMovies.SimilarMovie::getTitle)
                .collect(Collectors.toList())
        );
        
        movie = movieRepository.save(movie);
        processImages(movie, imagesFuture.get(), tmdbMovie);
        
        return movie;
    }

    private void processImages(Movie movie, TmdbMovieImages images, TmdbMovie tmdbMovie) throws IOException {
        if (tmdbMovie.getPosterPath() != null) {
            String posterPath = apiService.downloadImage(tmdbMovie.getPosterPath(), "poster", movie.getTitle());
            MovieImage posterImage = new MovieImage(posterPath, "poster");
            posterImage.setMovie(movie);
            movieImageRepository.save(posterImage);
        }
        
        if (tmdbMovie.getBackdropPath() != null) {
            String backdropPath = apiService.downloadImage(tmdbMovie.getBackdropPath(), "backdrop", movie.getTitle());
            MovieImage backdropImage = new MovieImage(backdropPath, "backdrop");
            backdropImage.setMovie(movie);
            movieImageRepository.save(backdropImage);
        }
        
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
            .orElseThrow(() -> new MovieNotFoundException(movieId));
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateRating(Long movieId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException();
        }
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new MovieNotFoundException(movieId));
        movie.setRating(rating);
        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
        movieRepository.deleteById(movieId);
    }
}