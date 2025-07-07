package MovieWatchlist.OOP3.service;

import MovieWatchlist.OOP3.dto.OmdbMovie;
import MovieWatchlist.OOP3.exception.OmdbApiException;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class OmdbClient {
 @Value("${omdb.api.key}")
 private String apiKey;
 
 private final ApiService apiService;
 
 public OmdbClient(ApiService apiService) {
     this.apiService = apiService;
 }
 
 public OmdbMovie fetchMovie(String title) {
     try {
         URI uri = new URIBuilder("http://www.omdbapi.com/")
             .addParameter("apikey", apiKey)
             .addParameter("t", title)
             .build();
         return apiService.executeGetRequest(uri, OmdbMovie.class);
     } catch (URISyntaxException e) {
         throw new OmdbApiException("Invalid URI for OMDb API", e);
     } catch (Exception e) {
         throw new OmdbApiException("Failed to fetch from OMDb API", e);
     }
 }
}
