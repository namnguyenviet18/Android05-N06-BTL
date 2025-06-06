package com.group06.music_app_mobile.api_client.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ExceptionResponse {
    private String message;
    private Map<String, String> errors;
}
