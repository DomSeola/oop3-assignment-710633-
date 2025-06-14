package MovieWatchlist.OOP3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OmdbMovie {
    @JsonProperty("Title")
    private String title;
    
    @JsonProperty("Year")
    private String year;
    
    @JsonProperty("Director")
    private String director;
    
    @JsonProperty("Genre")
    private String genre;
    
    @JsonProperty("imdbID")
    private String imdbId;
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
}