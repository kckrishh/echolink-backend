package com.echolink.backend.Services.Mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService {

    private final WebClient webClient;

    @Value("${brevo.senderEmail}")
    private String senderEmail;

    @Value("${brevo.senderName}")
    private String senderName;

    public BrevoEmailService(@Value("${brevo.apiKey}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendHtml(String toEmail, String subject, String html) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", html);

        webClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
