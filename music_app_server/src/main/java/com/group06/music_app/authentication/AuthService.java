package com.group06.music_app.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.group06.music_app.authentication.requests.AuthenticationRequest;
import com.group06.music_app.authentication.requests.GoogleAuthRequest;
import com.group06.music_app.authentication.requests.RegisterRequest;
import com.group06.music_app.authentication.requests.VerifyOtpRequest;
import com.group06.music_app.authentication.responses.AuthenticationResponse;
import com.group06.music_app.email.EmailService;
import com.group06.music_app.email.EmailTemplateName;
import com.group06.music_app.handler.custom_exception.*;
import com.group06.music_app.otp.OTP;
import com.group06.music_app.otp.OtpRepository;
import com.group06.music_app.security.JwtService;
import com.group06.music_app.user.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Value("${application.security.google.client-id}")
    private String clientId;

    public OTP register(RegisterRequest request) throws MessagingException {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if(userOptional.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = userMapper.fromUserRegisterRequest(request);
        userRepository.save(user);
        OTP otp = sendOTPEmail(user, "Music App Account Activation");
        return makeOtpResponse(otp);
    }

    public AuthenticationResponse authenticate(@Valid AuthenticationRequest request) throws AccessDeniedException {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if(userOptional.isEmpty()) {
            throw new AuthenticationException("Account does not exist");
        }
        User user = userOptional.get();
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Password is incorrect");
        }
        if(!user.isEnabled()) {
            throw new AccountNotApprovedException("Account not approved");
        }
        if(user.isAccountLocked()) {
            throw new AccessDeniedException("Account is locked");
        }
        return generateTokens(user);
    }

    private AuthenticationResponse generateTokens(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse verifyOTP(VerifyOtpRequest request) throws MessagingException {
        Optional<OTP> otpOption = otpRepository.findByTokenAndUserEmail(
                request.getOtp(),
                request.getEmail()
        );
        if(otpOption.isEmpty()) {
            throw new InvalidOtpException("Invalid OTP");
        }
        OTP otp = otpOption.get();
        if(LocalDateTime.now().isAfter(otp.getExpireAt())) {
            sendOTPEmail(otp.getUser(), "Music App");
            throw new InvalidOtpException("This OTP has expired. A new otp has been send to the same email address");
        }
        User user = otp.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        return generateTokens(otp.getUser());
    }

    private OTP sendOTPEmail(User user, String subject) throws MessagingException {
        OTP otp = generateAndSaveOTP(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                otp.getToken(),
                subject
        );
        return otp;
    }

    private OTP generateAndSaveOTP(User user) {
        String generateOTP = generateOTP(6);
        OTP otp = OTP.builder()
                .token(generateOTP)
                .issuedAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .build();
        return otpRepository.save(otp);
    }


    private String generateOTP(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }


    public OTP resendOtp(String email) throws MessagingException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("No account exists with this email");
        }
        User user = userOptional.get();
        return sendOTPEmail(user, "Music App");
    }

    private OTP makeOtpResponse(OTP otp) {
        return OTP.builder()
                .id(otp.getId())
                .token(null)
                .user(null)
                .issuedAt(otp.getIssuedAt())
                .expireAt(otp.getExpireAt())
                .build();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String email;
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        email = jwtService.extractUsername(refreshToken);
        if(email != null) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if(userOptional.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            User user = userOptional.get();
            if(jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .refreshToken(refreshToken)
                        .accessToken(accessToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public AuthenticationResponse authenticateWithGoogle(@Valid GoogleAuthRequest request) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken token = verifier.verify(request.getTokenId());
            if(token != null) {
                GoogleIdToken.Payload payload = token.getPayload();
                Optional<User> userOtp = userRepository.findByEmail(payload.getEmail());
                if(userOtp.isPresent() && userOtp.get().getLoginMethod() == LoginMethod.PASSWORD) {
                    throw new GoogleAuthException("Your account already exists, please login with email and password!");
                }
                User user = userMapper.fromGooglePayload(payload);
                if(userOtp.isEmpty()) {
                    user = userRepository.save(user);
                }
                return generateTokens(user);

            }
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleAuthException("An error occurred.!");
        }
        throw new GoogleAuthException("An error occurred.!");
    }
}
