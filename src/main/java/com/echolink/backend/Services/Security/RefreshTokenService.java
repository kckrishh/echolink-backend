package com.echolink.backend.Services.Security;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.echolink.backend.Entities.RefreshToken;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Repo.RefreshTokenRepo;
import com.echolink.backend.Repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, UserRepo userRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public String issueRefreshToken(String username) {
        User user = userRepo.findByEmail(username);

        refreshTokenRepo.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setTokenDisabled(false);

        refreshTokenRepo.save(refreshToken);
        return refreshToken.getToken();
    }

    public String validateAndGetUsername(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isTokenDisabled()) {
            throw new RuntimeException("Refresh token is disabled");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        return refreshToken.getUser().getEmail();
    }

    public void disableRefreshToken(String token) {
        refreshTokenRepo.findByToken(token).ifPresent(rt -> {
            rt.setTokenDisabled(true);
            refreshTokenRepo.save(rt);
        });
    }

    public String generateSecureToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
