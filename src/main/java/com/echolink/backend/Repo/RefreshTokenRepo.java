package com.echolink.backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.RefreshToken;
import com.echolink.backend.Entities.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
