package MovieWatchlist.OOP3.model;

import jakarta.persistence.*;

@Entity
public class MovieImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String filePath;
    private String imageType; // "poster", "backdrop", etc.
    
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;
    
    // Constructors, getters, and setters
    public MovieImage() {}
    
    public MovieImage(String filePath, String imageType) {
        this.filePath = filePath;
        this.imageType = imageType;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
}