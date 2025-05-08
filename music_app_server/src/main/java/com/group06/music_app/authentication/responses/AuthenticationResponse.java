package com.group06.music_app.authentication.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String refreshToken;
    private String accessToken;
}
