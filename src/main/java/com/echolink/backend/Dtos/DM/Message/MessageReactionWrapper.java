package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class MessageReactionWrapper {
    ReactionResponseDto dto;
    Long otherUserId;

    public MessageReactionWrapper(ReactionResponseDto dto, Long otherUserid) {
        this.dto = dto;
        this.otherUserId = otherUserid;
    }

    public MessageReactionWrapper() {
    }
}
