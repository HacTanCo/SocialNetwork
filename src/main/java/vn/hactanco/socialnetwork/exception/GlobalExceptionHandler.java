package vn.hactanco.socialnetwork.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	// fallback (tránh lộ stacktrace)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleOther(Exception ex) {
		return ResponseEntity.status(500).body("Đã xảy ra lỗi hệ thống");
	}
}
