package com.echolink.backend.Dtos.DM.Conversation;

import java.time.LocalDateTime;

import com.echolink.backend.Enums.MessageStatus;
import com.echolink.backend.Enums.ParticipantStatus;

import lombok.Data;

@Data
public class ConversationResponseDto {
    private Long conversationId;
    private String pairKey;
    private LocalDateTime lastMessagedAt;
    private String lastMessagePreview;
    private String target_username;
    private String target_userAvatar;
    private MessageStatus messageStatus;
}
