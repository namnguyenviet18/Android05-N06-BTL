package com.group06.music_app.authentication.requests;

import jakarta.validation.constraints.NotBlank;
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
public class ChangePasswordRequest {
    @NotEmpty(message = "New password is required")
    @NotBlank(message = "New password is required")
    @NotNull(message = "New password is required")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String newPassword;
    @NotEmpty(message = "Confirmation password is required")
    @NotBlank(message = "Confirmation password is required")
    @NotNull(message = "Confirmation password is required")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String confirmationPassword;
}
