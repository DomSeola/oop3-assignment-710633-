package MovieWatchlist.OOP3.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private int year;
    private String director;
    private String genre;
    private boolean watched;
    private int rating;
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieImage> images;
    
    @ElementCollection
    private List<String> similarMovies;
    
    // Constructors, getters, and setters
    public Movie() {}
    
    public Movie(String title, int year, String director, String genre) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.genre = genre;
        this.watched = false;
        this.rating = 0;
    }
    
    // Getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public boolean isWatched() { return watched; }
    public void setWatched(boolean watched) { this.watched = watched; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public List<MovieImage> getImages() { return images; }
    public void setImages(List<MovieImage> images) { this.images = images; }
    public List<String> getSimilarMovies() { return similarMovies; }
    public void setSimilarMovies(List<String> similarMovies) { this.similarMovies = similarMovies; }
}