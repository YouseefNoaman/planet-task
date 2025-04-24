package planettask.controller;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import planettask.model.ErrorResponse;
import planettask.util.NotFoundException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(fieldError -> fieldError.getDefaultMessage() != null)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        ErrorResponse errorResponse = buildErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                request);
        errorResponse.setValidationErrors(errors);

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildErrorResponse("Validation error: " + ex.getMessage(),
                        HttpStatus.BAD_REQUEST, request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {
        log.error("Data integrity violation", ex);
        String message = Optional.ofNullable(ex.getRootCause())
                .map(Throwable::getMessage)
                .map(msg -> msg.contains("duplicate key") ? "An entry with these details already exists" : msg)
                .orElse("Database constraint violation");
        
        return new ResponseEntity<>(
                buildErrorResponse(message, HttpStatus.CONFLICT, request),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        log.warn("Message not readable: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildErrorResponse("Invalid request body format",
                        HttpStatus.BAD_REQUEST, request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                Optional.ofNullable(ex.getRequiredType())
                        .map(Class::getSimpleName)
                        .orElse("unknown"));
        log.warn("Type mismatch: {}", message);
        return new ResponseEntity<>(
                buildErrorResponse(message, HttpStatus.BAD_REQUEST, request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        String message = String.format("Missing required parameter '%s'", ex.getParameterName());
        log.warn("Missing parameter: {}", message);
        return new ResponseEntity<>(
                buildErrorResponse(message, HttpStatus.BAD_REQUEST, request),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            WebRequest request) {
        log.warn("Response status exception: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return new ResponseEntity<>(
                buildErrorResponse(ex.getReason(), status, request),
                new HttpHeaders(),
                status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return new ResponseEntity<>(
                buildErrorResponse(
                        "An unexpected error occurred. Please try again later.",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        String path = request instanceof ServletWebRequest
                ? ((ServletWebRequest) request).getRequest().getRequestURI()
                : "Unknown";

        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
