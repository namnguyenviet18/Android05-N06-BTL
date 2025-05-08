package com.group06.music_app.security;

import com.group06.music_app.authentication.responses.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final JwtService jwtService;
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        String refreshToken = request.getHeader("REFRESH_TOKEN");

        if(authHeader == null || refreshToken == null) {
            System.out.println("Vao day 1");
            return;
        }
        if(authHeader.length() < 7 && refreshToken.length() < 7) {
            System.out.println("Vao day 2");
            return;
        }
        String accessToken = authHeader.substring(7);
        refreshToken = refreshToken.substring(7);

        jwtService.deleteTokenInRedis(accessToken);
        jwtService.deleteTokenInRedis(refreshToken);
        System.out.println("Vao day 3");
    }
}
