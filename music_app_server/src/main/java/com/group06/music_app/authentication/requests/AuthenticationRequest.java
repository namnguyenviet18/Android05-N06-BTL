package com.group06.music_app.authentication.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthenticationRequest {
    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is required")
    @NotNull(message = "Email is required")
    private String email;
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;
}
