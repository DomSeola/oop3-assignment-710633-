package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.OmdbMovie;
import MovieWatchlist.OOP3.dto.TmdbMovie;
import MovieWatchlist.OOP3.dto.TmdbMovieImages;
import MovieWatchlist.OOP3.dto.TmdbSimilarMovies;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ApiService {
    @Value("${omdb.api.key}")
    private String omdbApiKey;
    
    @Value("${tmdb.api.key}")
    private String tmdbApiKey;
    
    @Value("${image.download.path}")
    private String imageDownloadPath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CompletableFuture<OmdbMovie> fetchOmdbMovie(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder("http://www.omdbapi.com/")
                    .addParameter("apikey", omdbApiKey)
                    .addParameter("t", title)
                    .build();
                
                return executeGetRequest(uri, OmdbMovie.class);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to fetch from OMDb API", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovieSearchResult> searchTmdbMovie(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder("https://api.themoviedb.org/3/search/movie")
                    .addParameter("api_key", tmdbApiKey)
                    .addParameter("query", title)
                    .build();
                
                return executeGetRequest(uri, TmdbMovieSearchResult.class);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to search TMDB", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovie> getTmdbMovieDetails(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId)
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                
                return executeGetRequest(uri, TmdbMovie.class);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to fetch TMDB movie details", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovieImages> getTmdbMovieImages(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId + "/images")
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                
                return executeGetRequest(uri, TmdbMovieImages.class);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to fetch TMDB movie images", e);
            }
        });
    }
    
    public CompletableFuture<TmdbSimilarMovies> getTmdbSimilarMovies(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder("https://api.themoviedb.org/3/movie/" + tmdbId + "/similar")
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                
                return executeGetRequest(uri, TmdbSimilarMovies.class);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to fetch similar movies from TMDB", e);
            }
        });
    }
    
    private <T> T executeGetRequest(URI uri, Class<T> responseType) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(uri);
            String response = client.execute(request, httpResponse -> 
                EntityUtils.toString(httpResponse.getEntity()));
            
            return objectMapper.readValue(response, responseType);
        }
    }
    
    public String downloadImage(String imageUrl, String imageType, String movieTitle) throws IOException {
        // Create directory if it doesn't exist
        Files.createDirectories(Paths.get(imageDownloadPath));
        
        // Sanitize movie title for filename
        String sanitizedTitle = movieTitle.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = sanitizedTitle + "_" + imageType + "_" + System.currentTimeMillis() + ".jpg";
        String filePath = imageDownloadPath + File.separator + fileName;
        
        try (InputStream in = new URL("https://image.tmdb.org/t/p/original" + imageUrl).openStream();
             FileOutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        return filePath;
    }
}