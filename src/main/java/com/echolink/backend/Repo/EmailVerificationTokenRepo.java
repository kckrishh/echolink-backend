package com.echolink.backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.EmailVerificationToken;

public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
