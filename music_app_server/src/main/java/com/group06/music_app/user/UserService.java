package com.group06.music_app.user;

import com.group06.music_app.authentication.requests.ChangePasswordRequest;
import com.group06.music_app.handler.custom_exception.FileException;
import com.group06.music_app.song.SongService;
import com.group06.music_app.song.response.FileStoreResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final SongService songService;

    public void changePassword(ChangePasswordRequest request, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        if(!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalArgumentException("Password and confirmation password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getConfirmationPassword()));
        repository.save(user);
    }

    public UserResponse getProfile(Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        return mapper.toUserResponse(user);
    }

    public UserResponse editProfile(@NotNull EditProfileRequest request, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return mapper.toUserResponse(repository.save(user));
    }

    public UserResponse changeAvatar(MultipartFile avatar, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        if(avatar == null) {
            throw new FileException("File is required");
        }
        if(!Objects.requireNonNull(avatar.getContentType()).startsWith("image")) {
            throw new FileException("File must be image");
        }
        FileStoreResult result = songService.storeFile(avatar);
        user.setAvatarUrl(result.getFilePath());
        return mapper.toUserResponse(repository.save(user));
    }
}
