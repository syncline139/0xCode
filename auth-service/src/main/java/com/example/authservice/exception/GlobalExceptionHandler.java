package com.example.authservice.exception;

import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.exception.auth.EmailNotConfirmedException;
import com.example.authservice.exception.auth.IncorrectPasswordException;
import com.example.authservice.exception.auth.InvalidVerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsExceptions(EmailAlreadyExistsException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotConfirmedException.class)
    public ResponseEntity<String> handleEmailNotConfirmedExceptions(EmailNotConfirmedException ex) {

        String message = "Юзер не может войти в аккаунт т.к у него не подтверженный акканут";

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<String> handleIncorrectPasswordExceptions(IncorrectPasswordException ex) {

        return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleIncorrectPasswordExceptions(BadCredentialsException ex) {

        String message = "Пароль не совпадет";

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleIncorrectPasswordExceptions(DisabledException ex) {

        String message = "Нужно подтвердить почту при регистрации";

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<String> handleInvalidVerificationCodeExceptions(InvalidVerificationCodeException ex) {

        String message = "Неверный код для подтверждения акканут";

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
