package com.group06.music_app.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditProfileRequest {
    @NotBlank(message = "First name is required!")
    @NotEmpty(message = "First name is required!")
    private String firstName;
    @NotBlank(message = "Last name is required!")
    @NotEmpty(message = "Last name is required!")
    private String lastName;
}
