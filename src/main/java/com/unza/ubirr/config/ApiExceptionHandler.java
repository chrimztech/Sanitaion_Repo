package com.unza.ubirr.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

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
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload(exception.getMessage() != null ? exception.getMessage() : "Unexpected server error"));
  }

  private Map<String, Object> payload(String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", message);
    body.put("timestamp", Instant.now());
    return body;
  }
}
