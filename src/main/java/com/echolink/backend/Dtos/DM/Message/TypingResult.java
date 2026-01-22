package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class TypingResult {
    private Long OtherUserId;
    private TypingDto dto;
}
