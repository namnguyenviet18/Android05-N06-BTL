package com.group06.music_app.handler.custom_exception;


public class FileException extends RuntimeException {
    public FileException(String message) {
        super(message);
    }
}
