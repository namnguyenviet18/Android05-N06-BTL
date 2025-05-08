package com.group06.music_app.otp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group06.music_app.common.BaseEntity;
import com.group06.music_app.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"token", "user_id"})
})
public class OTP extends BaseEntity {

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private boolean isUsed = false;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
