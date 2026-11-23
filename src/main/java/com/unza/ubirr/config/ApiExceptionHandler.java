package com.unza.ubirr.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException exception) {
    return ResponseEntity.status(exception.getStatusCode())
      .body(payload(exception.getReason() != null ? exception.getReason() : "Request failed"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
    FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
    return ResponseEntity.badRequest().body(payload(message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception exception) {
    log.error("Unhandled exception", exception);
    // Never return exception.getMessage() here: it can leak internal details (field names,
    // file paths, SQL fragments, stack-trace fragments) to the client. Full details are logged
    // above for diagnosis; the client only ever sees a generic message.
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload("An unexpected error occurred. Please try again or contact support."));
  }

  private Map<String, Object> payload(String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", message);
    body.put("timestamp", Instant.now());
    return body;
  }
}
