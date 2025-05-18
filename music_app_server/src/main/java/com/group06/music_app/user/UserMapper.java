package com.group06.music_app.user;

import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .loginMethod(user.getLoginMethod())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .accountLocked(user.isAccountLocked())
                .build();
    }
}
