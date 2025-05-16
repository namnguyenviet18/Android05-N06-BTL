package com.group06.music_app.authentication.requests;
import jakarta.validation.constraints.*;
import lombok.*;

@Data // Tự động tạo getter, setter, toString, equals, hashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotEmpty(message = "Firstname is required")
    @NotBlank(message = "Firstname is required")
    @NotNull(message = "Firstname is required")
    private String firstName;
    @NotEmpty(message = "LastName is required")
    @NotBlank(message = "LastName is required")
    @NotNull(message = "LastName is required")
    private String lastName;
    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is required")
    @NotNull(message = "Email is required")
    private String email;
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    @NotBlank(message = "Password is required")
    private String password;
}
