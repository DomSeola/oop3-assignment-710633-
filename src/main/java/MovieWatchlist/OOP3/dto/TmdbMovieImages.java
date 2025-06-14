package MovieWatchlist.OOP3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TmdbMovieImages {
    @JsonProperty("posters")
    private List<Image> posters;
    
    @JsonProperty("backdrops")
    private List<Image> backdrops;
    
    public static class Image {
        @JsonProperty("file_path")
        private String filePath;
        
        // Getters and setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
    }
    
    // Getters and setters
    public List<Image> getPosters() { return posters; }
    public void setPosters(List<Image> posters) { this.posters = posters; }
    public List<Image> getBackdrops() { return backdrops; }
    public void setBackdrops(List<Image> backdrops) { this.backdrops = backdrops; }
}
