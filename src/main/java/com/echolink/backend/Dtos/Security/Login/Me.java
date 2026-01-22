package com.echolink.backend.Dtos.Security.Login;

import java.time.LocalDateTime;

import com.echolink.backend.Enums.SignUpState;

public record Me(Long id, String email, String username, String bio, String avatar, LocalDateTime joinedDate,
        SignUpState signUpState,
        int totalConversations, int totalBlockedPeople) {

}
