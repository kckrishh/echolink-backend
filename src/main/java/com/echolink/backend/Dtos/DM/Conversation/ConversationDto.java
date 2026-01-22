package com.echolink.backend.Dtos.DM.Conversation;

import java.time.LocalDateTime;

import com.echolink.backend.Enums.MessageStatus;
import com.echolink.backend.Enums.ParticipantStatus;

import lombok.Data;

@Data
public class ConversationDto {
    Long conversationId;
    Long otherUserId;
    String otherUsername;
    String otherUserAvatar;
    String lastMessagePreview;
    LocalDateTime lastMessageAt;
    ParticipantStatus status;
    int unreadCount;
    LocalDateTime lastReadAt;
    MessageStatus messageStatus;
}
