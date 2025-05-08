package com.group06.music_app.authentication.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotEmpty(message = "OTP is required")
    @NotBlank(message = "OTP is required")
    @NotNull(message = "OTP is required")
    private String otp;
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is required")
    @NotNull(message = "Email is required")
    private String email;
    private boolean isActivateAccount = true;
}
