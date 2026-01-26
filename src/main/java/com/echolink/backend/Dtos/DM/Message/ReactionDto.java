package com.echolink.backend.Dtos.DM.Message;

import com.echolink.backend.Enums.ReactionType;

import lombok.Data;

@Data
public class ReactionDto {
    private ReactionType type;
    private Long reactedById;
}
