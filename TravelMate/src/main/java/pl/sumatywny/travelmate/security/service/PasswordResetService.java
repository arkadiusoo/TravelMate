package pl.sumatywny.travelmate.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.security.dto.ForgotPasswordRequest;
import pl.sumatywny.travelmate.security.dto.PasswordResetRequest;
import pl.sumatywny.travelmate.security.dto.PasswordResetResponse;
import pl.sumatywny.travelmate.security.model.PasswordResetToken;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.repository.PasswordResetTokenRepository;
import pl.sumatywny.travelmate.security.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_VALIDITY_MINUTES = 15;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Initiate password reset process
     */
    @Transactional
    public PasswordResetResponse initiateForgotPassword(ForgotPasswordRequest request) {
        try {
            // Always return success message for security (don't reveal if email exists)
            PasswordResetResponse successResponse = PasswordResetResponse.builder()
                    .success(true)
                    .message("If an account with that email exists, we've sent you a password reset link")
                    .build();

            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isEmpty()) {
                // Don't reveal that email doesn't exist, but log it
                log.warn("Password reset requested for non-existent email: {}", request.getEmail());
                return successResponse;
            }

            User user = userOptional.get();

            // Generate secure token
            String token = generateSecureToken();

            // Create reset token entity
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES))
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);

            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), token);

            log.info("Password reset token generated for user: {}", user.getEmail());
            return successResponse;

        } catch (Exception e) {
            log.error("Error initiating password reset for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to process password reset request");
        }
    }

    /**
     * Reset password using token
     */
    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        try {
            // Find and validate token
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findValidToken(
                    request.getToken(), LocalDateTime.now()
            );

            if (tokenOptional.isEmpty()) {
                throw new IllegalArgumentException("Invalid or expired reset token");
            }

            PasswordResetToken resetToken = tokenOptional.get();
            User user = resetToken.getUser();

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Mark all tokens as used for this user (invalidate all reset tokens)
            tokenRepository.markAllAsUsedByUser(user);

            log.info("Password successfully reset for user: {}", user.getEmail());

            return PasswordResetResponse.builder()
                    .success(true)
                    .message("Password has been successfully reset")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("Password reset failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error resetting password with token: {}", request.getToken(), e);
            throw new RuntimeException("Failed to reset password");
        }
    }

    /**
     * Validate if a reset token is valid
     */
    public boolean isTokenValid(String token) {
        return tokenRepository.findValidToken(token, LocalDateTime.now()).isPresent();
    }

    /**
     * Generate cryptographically secure token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Cleanup expired tokens (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        int deletedCount = 0;
        try {
            tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Cleaned up {} expired password reset tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }
}