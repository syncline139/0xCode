package com.example.authservice.exception;

import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsExceptions(EmailAlreadyExistsException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

}
