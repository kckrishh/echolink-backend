package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class SendMessageResult {
    private MessageDto message;
    private boolean request;

    public SendMessageResult(MessageDto message, boolean request) {
        this.message = message;
        this.request = request;
    }
}
