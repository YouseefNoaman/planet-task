package planettask.controller;

import java.time.OffsetDateTime;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;
import planettask.util.NotFoundException;

@RestControllerAdvice
public class ControllerExceptionHandler {

  // Handle 400 - Validation Errors
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream().filter(fieldError -> fieldError.getDefaultMessage() != null)
        .collect(Collectors.toMap(
            FieldError::getField,
            DefaultMessageSourceResolvable::getDefaultMessage,
            (existing, replacement) -> existing // Keep first error in case of duplicates
        ));

    return ResponseEntity.badRequest().body(errors);
  }

  // Handle 400 - Constraint Violations
  @ExceptionHandler({NotFoundException.class, ConstraintViolationException.class})
  public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    return buildErrorResponse("Validation error: " + ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  // Handle 409 - Data Integrity Violation (e.g., unique constraint failure)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
    return buildErrorResponse("Database error: " + ex.getRootCause(), HttpStatus.CONFLICT, request);
  }

  // Handle 400 - Invalid Request Errors (e.g., ResponseStatusException)
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
    return buildErrorResponse(ex.getReason(), HttpStatus.valueOf(ex.getStatusCode().value()), request);
  }

  // Handle 500 - Generic Internal Server Errors
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
    return buildErrorResponse("An unexpected error occurred: " + ex.getCause(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
    String path = (request instanceof ServletWebRequest)
        ? ((ServletWebRequest) request).getRequest().getRequestURI()
        : "Unknown";

    Map<String, Object> errorResponse = Map.of(
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message,
        "path", path,
        "timestamp", OffsetDateTime.now()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }

}
