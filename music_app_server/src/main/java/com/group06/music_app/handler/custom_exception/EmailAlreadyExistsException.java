package com.group06.music_app.handler.custom_exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super((message));
    }
    /// HTTP STATUS CODE 409 Conflict
}
