package MovieWatchlist.OOP3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
 
 @ExceptionHandler(MovieNotFoundException.class)
 public ResponseEntity<ErrorResponse> handleMovieNotFound(MovieNotFoundException ex) {
     return ResponseEntity.status(HttpStatus.NOT_FOUND)
         .body(new ErrorResponse(ex.getMessage()));
 }
 
 @ExceptionHandler(InvalidRatingException.class)
 public ResponseEntity<ErrorResponse> handleInvalidRating(InvalidRatingException ex) {
     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
         .body(new ErrorResponse(ex.getMessage()));
 }
 
 @ExceptionHandler({OmdbApiException.class, TmdbApiException.class})
 public ResponseEntity<ErrorResponse> handleApiExceptions(RuntimeException ex) {
     return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
         .body(new ErrorResponse("External API error: " + ex.getMessage()));
 }
 
 private record ErrorResponse(String message) {}
}