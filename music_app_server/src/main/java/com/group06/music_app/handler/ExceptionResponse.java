package com.group06.music_app.handler;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NotNull
public class ExceptionResponse {
    private String message;
    private Map<String, String> errors;
}
