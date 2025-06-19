package pl.sumatywny.travelmate.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - TravelMate");
            helper.setFrom("noreply@travelmate.com"); // You can use your Gmail here too
            helper.setText(buildEmailContent(resetLink), true);

            // Send the actual email
            javaMailSender.send(message);

            log.info("✅ Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailContent(String resetLink) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset - TravelMate</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">🌍 TravelMate</h1>
                    <p style="color: white; margin: 10px 0 0 0; opacity: 0.9;">Your travel companion</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #ddd; border-top: none;">
                    <h2 style="color: #333; margin-top: 0;">Password Reset Request</h2>
                    
                    <p>Hello!</p>
                    
                    <p>We received a request to reset your password for your TravelMate account. If you didn't make this request, please ignore this email.</p>
                    
                    <p>To reset your password, click the button below:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="RESET_LINK_PLACEHOLDER" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; display: inline-block; font-size: 16px;">Reset My Password</a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="background: #e9e9e9; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 14px;">RESET_LINK_PLACEHOLDER</p>
                    
                    <p style="color: #666; font-size: 14px; margin-top: 30px;">
                        <strong>Security note:</strong> This link will expire in 1 hour for your security.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="color: #888; font-size: 12px; text-align: center;">
                        This email was sent by TravelMate. If you have any questions, please contact our support team.
                    </p>
                </div>
            </body>
            </html>
            """;

        return template.replace("RESET_LINK_PLACEHOLDER", resetLink);
    }
}