package com.group06.music_app.handler;

import com.group06.music_app.handler.custom_exception.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountLockedException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Tag(name = "Exception")
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            EmailAlreadyExistsException exp
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            AuthenticationException exp
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(AccountNotApprovedException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            AccountNotApprovedException exp
    ) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            AccountLockedException exp
    ) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            InvalidOtpException exp
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            UserNotFoundException exp
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            EntityNotFoundException exp
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }


    @ExceptionHandler(GoogleAuthException.class)
    public ResponseEntity<ExceptionResponse> handlerException(
            GoogleAuthException exp
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .message(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(
            MethodArgumentNotValidException exp
    ) {
        Map<String, String> errors = exp.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid"),
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .message("Invalid information")
                        .errors(errors)
                        .build());
    }
}
