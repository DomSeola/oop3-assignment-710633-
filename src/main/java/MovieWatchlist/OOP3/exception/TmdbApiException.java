package MovieWatchlist.OOP3.exception;

public class TmdbApiException extends RuntimeException {
    public TmdbApiException(String message, Throwable cause) {
        super(message, cause);
    }
}