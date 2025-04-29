package com.group06.music_app_mobile.models;

import com.group06.music_app_mobile.models.enums.LoginMethod;
import com.group06.music_app_mobile.models.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String avatarUrl;
    private LoginMethod loginMethod;
    private Role role;
    private boolean enabled;
    private boolean accountLocked;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
