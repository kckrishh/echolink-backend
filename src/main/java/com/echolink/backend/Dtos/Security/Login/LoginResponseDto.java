package com.echolink.backend.Dtos.Security.Login;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String jwt;

    public LoginResponseDto(String jwt) {
        this.jwt = jwt;
    }
}
