package com.group06.music_app.authentication.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthRequest {
    @NotEmpty(message = "Token ID is required")
    @NotBlank(message = "Token ID is required")
    private String tokenId;
}
