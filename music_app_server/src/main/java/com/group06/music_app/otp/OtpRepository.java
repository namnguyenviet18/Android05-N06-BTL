package com.group06.music_app.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OTP, Long> {

    @Query("SELECT o FROM OTP o WHERE o.isUsed = false AND o.token = :token AND o.user.email = :email")
    Optional<OTP> findByTokenAndUserEmail(
            @Param("token") String token,
            @Param("email") String email
    );
}
