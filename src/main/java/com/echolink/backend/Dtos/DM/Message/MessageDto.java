package com.echolink.backend.Dtos.DM.Message;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageDto {
    Long messageId;
    Long conversationId;
    String content;
    LocalDateTime createdAt;
    Long senderId;
    String senderUsername;
    String senderAvatar;
}
