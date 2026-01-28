package com.echolink.backend.Services.Mail;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final BrevoEmailService brevo;

    public EmailService(BrevoEmailService brevo) {
        this.brevo = brevo;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = "https://YOUR_BACKEND/auth/confirm?token=" + token;

        String html = """
                  <div style="font-family: Arial">
                    <h2>Verify your email</h2>
                    <p><a href="%s">Verify Email</a></p>
                  </div>
                """.formatted(link);

        brevo.sendHtml(toEmail, "Verify your EchoLink account", html);
    }
}
