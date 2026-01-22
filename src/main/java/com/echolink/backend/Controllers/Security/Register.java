package com.echolink.backend.Controllers.Security;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.echolink.backend.Dtos.Security.Register.CompleteProfileRequestDto;
import com.echolink.backend.Dtos.Security.Register.RegisterStartRequestDto;
import com.echolink.backend.Dtos.Security.Register.VerifyEmailRequestDto;
import com.echolink.backend.Entities.EmailVerificationToken;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.SignUpState;
import com.echolink.backend.Repo.EmailVerificationTokenRepo;
import com.echolink.backend.Repo.UserRepo;
import com.echolink.backend.Services.Mail.SmtpEmailService;

import jakarta.transaction.Transactional;

@RequestMapping("/auth")
@RestController
public class Register {

    private final UserRepo userRepo;
    private final EmailVerificationTokenRepo tokenRepo;
    private final SmtpEmailService mailService;

    public Register(UserRepo userRepo, EmailVerificationTokenRepo tokenRepo, SmtpEmailService mailService) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.mailService = mailService;
    }

    @PostMapping("/register-start")
    public void registerAccount(@RequestBody RegisterStartRequestDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists. Please use a different email!!");
        }

        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(dto.password());
        user.setSignUpState(SignUpState.PENDING_EMAIL_VERIFICATION);
        user = userRepo.save(user);

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        tokenRepo.deleteByUserId(user.getId());

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setCodeHash(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        tokenRepo.save(token);

        mailService.sendVerificationCode(email, code);
    }

    @PostMapping("/verify-email")
    @Transactional
    public void verifyEmail(@RequestBody VerifyEmailRequestDto dto) {
        if (dto.email() == null || dto.email().isBlank() || dto.code() == null || dto.code().isBlank())
            throw new IllegalArgumentException("Email and code are required");

        String email = dto.email().trim().toLowerCase();

        User user = userRepo.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (user.getSignUpState() != SignUpState.PENDING_EMAIL_VERIFICATION) {
            throw new IllegalArgumentException("Email is already verified or signup state is invalid");
        }

        EmailVerificationToken token = tokenRepo.findByUserId(user.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Verification code not found. Please request a new code."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepo.deleteByUserId(user.getId());
            throw new IllegalArgumentException("Verification code expired. Please request a new code.");
        }

        if (!dto.code().trim().equals(token.getCodeHash())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        tokenRepo.deleteByUserId(user.getId());
        user.setSignUpState(SignUpState.PENDING_PROFILE);
        userRepo.save(user);
    }

    @PostMapping("/complete-profile")
    public void completeProfile(@RequestBody CompleteProfileRequestDto dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (dto.username() == null || dto.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        String email = dto.email().trim().toLowerCase();
        String username = dto.username().trim();

        if (!username.matches("^[a-zA-Z0-9_\\.]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username can only contain letters, numbers, underscore, and dot");
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (user.getSignUpState() != SignUpState.PENDING_PROFILE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid signup state");
        }

        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        String avatar = (dto.avatar() == null || dto.avatar().isBlank()) ? null : dto.avatar().trim();

        user.setUsername(username);
        user.setBio(dto.bio());
        user.setAvatar(avatar);
        user.setSignUpState(SignUpState.COMPLETE);

        userRepo.save(user);

    }

}
