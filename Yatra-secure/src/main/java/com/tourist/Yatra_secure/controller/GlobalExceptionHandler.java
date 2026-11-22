package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.dto.AuthResponses;
import com.tourist.Yatra_secure.service.AuthException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    ResponseEntity<AuthResponses.Error> auth(AuthException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new AuthResponses.Error(ex.getStatus().getReasonPhrase(), ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AuthResponses.Error> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField()+": "+e.getDefaultMessage()).distinct().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new AuthResponses.Error("Bad Request", message));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<AuthResponses.Error> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new AuthResponses.Error("Bad Request", "Request data is missing or invalid"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<AuthResponses.Error> duplicate(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponses.Error("Conflict", "Email is already registered"));
    }
}
