package com.group06.music_app_mobile.models;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OTP extends BaseEntity {
    private String token;
    private boolean isUsed = false;
    private LocalDateTime issuedAt;
    private LocalDateTime expireAt;
    private User user;
}
