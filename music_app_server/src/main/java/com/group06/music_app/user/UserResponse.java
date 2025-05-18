package com.group06.music_app.user;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
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
}
