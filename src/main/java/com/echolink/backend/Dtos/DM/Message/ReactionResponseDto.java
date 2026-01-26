package com.echolink.backend.Dtos.DM.Message;

import com.echolink.backend.Enums.ReactionType;

import lombok.Data;

@Data
public class ReactionResponseDto {
    private Long conversationId;
    private Long messageId;
    private ReactionType type;
    private Long reactedBy;
    private String action;

}
