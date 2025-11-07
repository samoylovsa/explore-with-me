package stats.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    HttpStatus httpStatus;
    String message;
    String stackTrace;
}
