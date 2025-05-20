package com.group06.music_app.handler.custom_exception;

public class GoogleAuthException extends RuntimeException {
    public GoogleAuthException(String message) {
        super(message);
    }
}
