package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.*;
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
import java.util.concurrent.CompletableFuture;

@Service
public class ApiService {
    @Value("${omdb.api.key}")
    private String omdbApiKey;
    
    @Value("${tmdb.api.key}")
    private String tmdbApiKey;
    
    @Value("${image.download.path}")
    private String imageDownloadPath;
    
    @Value("${omdb.api.url}")
    private String omdbApiUrl;
    
    @Value("${tmdb.api.url}")
    private String tmdbApiUrl;
    
    @Value("${tmdb.image.url}")
    private String tmdbImageUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CompletableFuture<OmdbMovie> fetchOmdbMovie(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder(omdbApiUrl)
                    .addParameter("apikey", omdbApiKey)
                    .addParameter("t", title)
                    .build();
                return executeGetRequest(uri, OmdbMovie.class);
            } catch (Exception e) {
                throw new RuntimeException("OMDb API error", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovieSearchResult> searchTmdbMovie(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder(tmdbApiUrl + "search/movie")
                    .addParameter("api_key", tmdbApiKey)
                    .addParameter("query", title)
                    .build();
                return executeGetRequest(uri, TmdbMovieSearchResult.class);
            } catch (Exception e) {
                throw new RuntimeException("TMDB search error", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovie> getTmdbMovieDetails(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder(tmdbApiUrl + "movie/" + tmdbId)
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                return executeGetRequest(uri, TmdbMovie.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch TMDB movie details", e);
            }
        });
    }
    
    public CompletableFuture<TmdbMovieImages> getTmdbMovieImages(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder(tmdbApiUrl + "movie/" + tmdbId + "/images")
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                return executeGetRequest(uri, TmdbMovieImages.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch TMDB movie images", e);
            }
        });
    }
    
    public CompletableFuture<TmdbSimilarMovies> getTmdbSimilarMovies(int tmdbId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URIBuilder(tmdbApiUrl + "movie/" + tmdbId + "/similar")
                    .addParameter("api_key", tmdbApiKey)
                    .build();
                return executeGetRequest(uri, TmdbSimilarMovies.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch similar movies from TMDB", e);
            }
        });
    }
    
    private <T> T executeGetRequest(URI uri, Class<T> responseType) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(uri);
            return client.execute(request, httpResponse -> {
                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                return objectMapper.readValue(responseBody, responseType);
            });
        }
    }
    
    public String downloadImage(String imagePath, String imageType, String movieTitle) throws IOException {
        Files.createDirectories(Paths.get(imageDownloadPath));
        String sanitizedTitle = movieTitle.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = sanitizedTitle + "_" + imageType + "_" + System.currentTimeMillis() + ".jpg";
        String filePath = imageDownloadPath + File.separator + fileName;
        
        try (InputStream in = new URL(tmdbImageUrl + imagePath).openStream();
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