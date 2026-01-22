package com.echolink.backend.Dtos.DM.Conversation;

import lombok.Data;

@Data
public class ConversationRequestDto {
    String username;

    public ConversationRequestDto(String username) {
        this.username = username;
    }

    public ConversationRequestDto() {
    }
}
