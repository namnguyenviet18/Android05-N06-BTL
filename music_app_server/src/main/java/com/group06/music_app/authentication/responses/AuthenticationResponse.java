package com.group06.music_app.authentication.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String refreshToken;
    private String accessToken;
}
