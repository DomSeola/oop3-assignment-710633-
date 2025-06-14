package MovieWatchlist.OOP3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TmdbMovieSearchResult {
    @JsonProperty("results")
    private List<TmdbMovie> results;
    
    public List<TmdbMovie> getResults() { return results; }
    public void setResults(List<TmdbMovie> results) { this.results = results; }
}