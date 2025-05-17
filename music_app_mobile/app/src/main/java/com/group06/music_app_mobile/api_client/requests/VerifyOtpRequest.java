package com.group06.music_app_mobile.api_client.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    private String otp;
    private String email;
    private boolean isActivateAccount = true;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivateAccount() {
        return isActivateAccount;
    }

    public void setActivateAccount(boolean activateAccount) {
        isActivateAccount = activateAccount;
    }
}
