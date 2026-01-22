package com.echolink.backend.Dtos.People;

import java.time.LocalDateTime;

// import com.echolink.backend.Enums.ParticipantStatus;

import com.echolink.backend.Enums.ParticipantStatus;

import lombok.Data;

@Data
public class SearchPeopleResponseDto {
    private String username;
    private LocalDateTime createdAt;
    private Long conversationId;
    private ParticipantStatus status;
    private String bio;
    private String avatar;
}
