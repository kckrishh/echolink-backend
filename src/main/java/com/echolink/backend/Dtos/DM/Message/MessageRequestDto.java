package com.echolink.backend.Dtos.DM.Message;

import lombok.Data;

@Data
public class MessageRequestDto {
    public String targetUsername;
    public String text;
}
