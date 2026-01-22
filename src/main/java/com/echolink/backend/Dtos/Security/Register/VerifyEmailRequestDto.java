package com.echolink.backend.Dtos.Security.Register;

public record VerifyEmailRequestDto(String email, String code) {
}