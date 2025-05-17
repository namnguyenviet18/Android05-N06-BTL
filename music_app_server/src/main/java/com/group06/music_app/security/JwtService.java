package com.group06.music_app.security;

import com.group06.music_app.configs.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long jwtRefreshExpiration;

    private final RedisService redisService;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(
                new HashMap<>(),
                userDetails,
                jwtRefreshExpiration
        );
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long jwtExpiration
    ) {
        var authorities = userDetails.getAuthorities();
        String email = userDetails.getUsername();
        String tokenId = String.valueOf(UUID.randomUUID());
        Date expiredAt = new Date(System.currentTimeMillis() + jwtExpiration);
        String token = Jwts.builder()
                .claims(extraClaims)
                .id(email + tokenId)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiredAt)
                .claim("authorities", authorities)
                .signWith(getSignInKey())
                .compact();
        redisService.saveDate(email + tokenId, expiredAt);
        return token;
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tokenId = extractTokenId(token);
        var inRedis = redisService.getData(tokenId);
        if(inRedis == null) {
            return false;
        }
        boolean isExpired = isTokenExpired(token);
        if(isExpired || !username.equals(userDetails.getUsername())) {
            redisService.deleteData(tokenId);
            return false;
        }
        return true;
    }

    public void deleteTokenInRedis(String token) {
        if(token == null) {
            return;
        }
        final String tokenId = extractTokenId(token);
        redisService.deleteData(tokenId);
    }

    public void deleteTokenContainEmail(String email) {
        redisService.deleteKeysContainingEmail(email);
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
