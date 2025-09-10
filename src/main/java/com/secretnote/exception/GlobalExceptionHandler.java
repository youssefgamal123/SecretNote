package com.secretnote.exception;

import com.secretnote.dto.response.APIExceptionResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIExceptionResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new APIExceptionResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIExceptionResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new APIExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND));
    }


    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<APIExceptionResponse> handleSecurityException(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new APIExceptionResponse(ex.getMessage(), HttpStatus.FORBIDDEN));
    }






    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIExceptionResponse> handleRuntimeException(RuntimeException ex) {
        APIExceptionResponse response = new APIExceptionResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);




    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIExceptionResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new APIExceptionResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIExceptionResponse> handleGeneralException(Exception ex) {
        APIExceptionResponse response = new APIExceptionResponse(
                "Unexpected error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
