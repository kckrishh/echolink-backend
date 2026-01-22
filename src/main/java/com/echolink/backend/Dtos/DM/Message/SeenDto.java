package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class SeenDto {
    private Long conversationId;
    private Long lastReadMessageId;
    private Long seenByUserId;
    private Long otherUserId;
}
