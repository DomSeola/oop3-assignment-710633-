package MovieWatchlist.OOP3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TmdbSimilarMovies {
    @JsonProperty("results")
    private List<SimilarMovie> results;
    
    public static class SimilarMovie {
        private int id;
        private String title;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
    
    public List<SimilarMovie> getResults() { return results; }
    public void setResults(List<SimilarMovie> results) { this.results = results; }
}