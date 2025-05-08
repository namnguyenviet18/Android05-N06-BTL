package com.group06.music_app_mobile.api_client.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String newPassword;
    private String confirmationPassword;
}
