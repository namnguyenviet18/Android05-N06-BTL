package com.group06.music_app.user;

import com.group06.music_app.authentication.requests.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(ChangePasswordRequest request, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        if(!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalArgumentException("Password and confirmation password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getConfirmationPassword()));
        repository.save(user);
    }
}
