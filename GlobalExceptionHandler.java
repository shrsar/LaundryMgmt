package com.laundrymgmt.modern.exception;

import com.laundrymgmt.modern.dto.CommonDtos;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<CommonDtos.ErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(new CommonDtos.ErrorResponse(exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDtos.ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new CommonDtos.ErrorResponse(message, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonDtos.ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new CommonDtos.ErrorResponse(exception.getMessage(), Instant.now()));
    }
}
