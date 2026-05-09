package com.example.authservice.exception;

import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.exception.auth.EmailNotConfirmedException;
import com.example.authservice.exception.auth.IncorrectPasswordException;
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

    @ExceptionHandler(EmailNotConfirmedException.class)
    public ResponseEntity<String> handleEmailNotConfirmedExceptions(EmailNotConfirmedException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<String> handleIncorrectPasswordExceptions(IncorrectPasswordException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

}
