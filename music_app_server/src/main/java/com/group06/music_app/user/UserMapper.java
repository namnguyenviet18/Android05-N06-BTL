package com.group06.music_app.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.group06.music_app.authentication.requests.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

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

    public User fromUserRegisterRequest(RegisterRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .loginMethod(LoginMethod.PASSWORD)
                .build();
    }

    public User fromGooglePayload(GoogleIdToken.Payload payload) {

        String firstName = (String) (payload.get("given_name") != null ? payload.get("given_name") : "");
        String lastName = (String) (payload.get("family_name") != null ? payload.get("family_name") : "");
        String email = payload.getEmail();
        String avatarUrl = (String) payload.get("picture");

        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(email);
        System.out.println(avatarUrl);

        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .avatarUrl(avatarUrl)
                .password("")
                .role(Role.USER)
                .loginMethod(LoginMethod.GOOGLE)
                .enabled(true)
                .accountLocked(false)
                .build();
    }
}
