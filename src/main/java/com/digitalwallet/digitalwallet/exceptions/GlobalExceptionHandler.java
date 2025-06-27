package com.digitalwallet.digitalwallet.exceptions;

import com.digitalwallet.digitalwallet.models.DigitalWalletResponse;
import com.digitalwallet.digitalwallet.models.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.digitalwallet.digitalwallet.controllers")
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleUnauthorized(InvalidUserException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(DigitalWalletResponse.builder()
                        .status(false)
                        .errorDTO(new ErrorDTO("Unauthorized"))
                        .build()
                );
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleExceptions(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(DigitalWalletResponse.builder()
                        .status(false)
                        .errorDTO(new ErrorDTO(e.getMessage()))
                        .build()
                );
    }
}
