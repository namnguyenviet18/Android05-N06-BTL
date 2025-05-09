package com.group06.music_app.authentication;

import com.group06.music_app.authentication.requests.AuthenticationRequest;
import com.group06.music_app.authentication.requests.RegisterRequest;
import com.group06.music_app.authentication.requests.VerifyOtpRequest;
import com.group06.music_app.authentication.responses.AuthenticationResponse;
import com.group06.music_app.otp.OTP;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<OTP> register(
            @RequestBody @Valid RegisterRequest request
    ) throws MessagingException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) throws AccessDeniedException {
        System.out.println("vao day");
       return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOTP(
            @RequestBody VerifyOtpRequest request
    ) throws MessagingException {
        return ResponseEntity.ok(service.verifyOTP(request));
    }

    @GetMapping("/resend-otp/{email}")
    public ResponseEntity<OTP> resendOTP(
            @PathVariable(name = "email") String email
    ) throws MessagingException {
        return ResponseEntity.ok(service.resendOtp(email));
    }

    @GetMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

}
