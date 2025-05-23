package com.group06.music_app.user;

import com.group06.music_app.authentication.requests.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final UserService service;

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication currentUser
    ) {
        service.changePassword(request, currentUser);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            Authentication currentUser
    ) {
        return ResponseEntity.ok(service.getProfile(currentUser));
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<UserResponse> editProfile(
        @RequestBody @NotNull EditProfileRequest request,
        Authentication currentUser
    ) {
        return ResponseEntity.ok(service.editProfile(request, currentUser));
    }

    @PatchMapping("/change-avatar")
    public ResponseEntity<UserResponse> changeAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            Authentication currentUser
    ) {
        return ResponseEntity.ok(service.changeAvatar(avatar, currentUser));
    }
    
}
