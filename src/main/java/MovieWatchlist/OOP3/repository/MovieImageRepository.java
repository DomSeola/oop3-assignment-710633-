package MovieWatchlist.OOP3.repository;

import MovieWatchlist.OOP3.model.MovieImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieImageRepository extends JpaRepository<MovieImage, Long> {
}