package MovieWatchlist.OOP3.exception;

public class OmdbApiException extends RuntimeException {
 public OmdbApiException(String message, Throwable cause) {
     super(message, cause);
 }
}