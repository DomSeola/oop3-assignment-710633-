package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
import MovieWatchlist.OOP3.exception.TmdbApiException;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class TmdbClient {
 @Value("${tmdb.api.key}")
 private String apiKey;
 
 private final ApiService apiService;
 
 public TmdbClient(ApiService apiService) {
     this.apiService = apiService;
 }
 
 public TmdbMovieSearchResult searchMovie(String title) {
     try {
         URI uri = new URIBuilder("https://api.themoviedb.org/3/search/movie")
             .addParameter("api_key", apiKey)
             .addParameter("query", title)
             .build();
         return apiService.executeGetRequest(uri, TmdbMovieSearchResult.class);
     } catch (URISyntaxException e) {
         throw new TmdbApiException("Invalid URI for TMDB API", e);
     } catch (Exception e) {
         throw new TmdbApiException("Failed to search TMDB", e);
     }
 }
 
 public TmdbMovie getMovieDetails(int tmdbId) {
     try {
         URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId)
             .addParameter("api_key", apiKey)
             .build();
         return apiService.executeGetRequest(uri, TmdbMovie.class);
     } catch (Exception e) {
         throw new TmdbApiException("Failed to fetch TMDB movie details", e);
     }
 }
 
 public TmdbMovieImages getMovieImages(int tmdbId) {
     try {
         URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId + "/images")
             .addParameter("api_key", apiKey)
             .build();
         return apiService.executeGetRequest(uri, TmdbMovieImages.class);
     } catch (Exception e) {
         throw new TmdbApiException("Failed to fetch TMDB movie images", e);
     }
 }
 
 public TmdbSimilarMovies getSimilarMovies(int tmdbId) {
     try {
         URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId + "/similar")
             .addParameter("api_key", apiKey)
             .build();
         return apiService.executeGetRequest(uri, TmdbSimilarMovies.class);
     } catch (Exception e) {
         throw new TmdbApiException("Failed to fetch similar movies from TMDB", e);
     }
 }
}