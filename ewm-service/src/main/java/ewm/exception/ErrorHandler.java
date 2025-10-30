package ewm.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        log.warn("Error 500: {}", ex.getMessage(), ex);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), stackTrace);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .orElse("Validation failed");

        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format(
                "Failed to convert value of type %s to required type %s; %s",
                ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null",
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()
        );

        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> String.format("Field: %s. Error: %s. Value: %s",
                        violation.getPropertyPath(), violation.getMessage(), violation.getInvalidValue()))
                .orElse("Constraint violation");

        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessRule(BusinessRuleException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException ex) {
        return new ApiError(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
}
