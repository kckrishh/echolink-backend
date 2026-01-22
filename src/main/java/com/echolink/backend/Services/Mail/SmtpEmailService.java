package com.echolink.backend.Services.Mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SmtpEmailService {
  private final JavaMailSender mailSender;

  public SmtpEmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendVerificationCode(String toEmail, String code) {
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject("Verify your EchoLink account");
      helper.setFrom("EchoLink <no-reply@echolink.com>");

      helper.setText(buildHtml(code), true); // true = HTML

      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }
  }

  private String buildHtml(String code) {
    return """
        <!DOCTYPE html>
        <html>
        <body style="margin:0; padding:0; background:#0b0f14; font-family:Arial, sans-serif;">
          <div style="max-width:480px; margin:40px auto; background:#121826;
                      border-radius:16px; padding:32px; color:#ffffff;">

            <h2 style="margin-top:0;">Welcome to EchoLink 👋</h2>

            <p style="color:#b3b8c4;">
              Use the verification code below to confirm your email address.
            </p>

            <div style="margin:32px 0; text-align:center;">
              <span style="
                display:inline-block;
                background:#1f2937;
                padding:16px 32px;
                border-radius:12px;
                font-size:28px;
                letter-spacing:4px;
                font-weight:600;
              ">
                %s
              </span>
            </div>

            <p style="color:#b3b8c4; font-size:14px;">
              This code expires in <strong>10 minutes</strong>.
              If you didn’t request this, you can safely ignore this email.
            </p>

            <hr style="border:none; border-top:1px solid #1f2937; margin:24px 0;">

            <p style="font-size:12px; color:#6b7280;">
              © EchoLink • Secure conversations, connected.
            </p>
          </div>
        </body>
        </html>
        """.formatted(code);
  }
}