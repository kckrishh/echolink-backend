package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class TypingDto {
    private Long conversationId;
    private boolean typing;
}
