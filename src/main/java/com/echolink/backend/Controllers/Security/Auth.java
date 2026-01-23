package com.echolink.backend.Controllers.Security;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echolink.backend.Configs.Security.CustomAuthenticationProvider;
import com.echolink.backend.Configs.Security.CustomUserDetailsManager;
import com.echolink.backend.Configs.Security.JWTUtil;
import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Dtos.Security.Login.LoginRequestDto;
import com.echolink.backend.Dtos.Security.Login.LoginResponseDto;
import com.echolink.backend.Dtos.Security.Login.Me;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.ParticipantStatus;
import com.echolink.backend.Repo.DMParticipantRepo;
import com.echolink.backend.Repo.UserRepo;
import com.echolink.backend.Services.Security.RefreshTokenService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class Auth {
    private final CustomAuthenticationProvider authenticationProvider;
    private final UserRepo userRepo;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsManager userDetailsManager;
    private final HelperMethods helperMethods;
    private final DMParticipantRepo participantRepo;

    public Auth(CustomAuthenticationProvider authenticationProvider, UserRepo userRepo,
            RefreshTokenService refreshTokenService, CustomUserDetailsManager userDetailsManager,
            HelperMethods helperMethods, DMParticipantRepo participantRepo) {
        this.authenticationProvider = authenticationProvider;
        this.userRepo = userRepo;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsManager = userDetailsManager;
        this.helperMethods = helperMethods;
        this.participantRepo = participantRepo;
    }

    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response) {
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = JWTUtil.generateToken(userDetails);
        String refreshToken = refreshTokenService.issueRefreshToken(userDetails.getUsername());
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }

    @GetMapping("token/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        System.out.println("request reached");
        System.out.println("REFRESH COOKIE RECEIVED: " + refreshToken);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(("Missing refresh token"));
        }
        String username;
        try {
            username = refreshTokenService.validateAndGetUsername(refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
        System.out.println("REFRESH TOKEN VALID FOR USER: " + username);

        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        String newAccessToken = JWTUtil.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponseDto(newAccessToken));
    }

    @PostMapping("token/logout")
    public ResponseEntity<?> logout(HttpServletResponse response,
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.disableRefreshToken(refreshToken);
        }
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public Me getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(user);

        List<DMParticipant> participants = participantRepo.findAllByUser(user);
        int totalConversations = 0;

        int totalBlockedPeople = 0;

        for (DMParticipant myParticipant : participants) {
            if (myParticipant.getStatus() == ParticipantStatus.PENDING
                    || myParticipant.getStatus() == ParticipantStatus.NEUTRAL) {
                continue;
            }
            totalConversations++;

        }

        Me me = new Me(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getAvatar(),
                user.getCreatedAt(),
                user.getSignUpState(), totalConversations, totalBlockedPeople);
        return me;
    }
}
