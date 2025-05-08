package com.group06.music_app_mobile.api_client.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    private String otp;
    private String email;
    private boolean isActivateAccount = true;
}
