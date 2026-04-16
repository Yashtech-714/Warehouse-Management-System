package com.wms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "RESOURCE_NOT_FOUND");
		body.put("message", exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
	public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "BAD_REQUEST");
		body.put("message", exception.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "VALIDATION_FAILED");
		body.put("message", "Request validation failed.");
		body.put("details", exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField,
						f -> f.getDefaultMessage() == null ? "Invalid value" : f.getDefaultMessage(),
						(existing, replacement) -> existing)));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "BAD_REQUEST");
		body.put("message", "Invalid parameter type: " + exception.getName());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "NOT_FOUND");
		body.put("message", "Resource not found. Open the frontend URL (http://localhost:5173 or 5174) for UI.");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "INTERNAL_SERVER_ERROR");
		body.put("message", exception.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
