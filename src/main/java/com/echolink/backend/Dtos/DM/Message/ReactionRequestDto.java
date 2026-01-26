package com.echolink.backend.Dtos.DM.Message;

import com.echolink.backend.Enums.ReactionType;

import lombok.Data;

@Data
public class ReactionRequestDto {
    private Long messageId;
    private ReactionType type;
}
