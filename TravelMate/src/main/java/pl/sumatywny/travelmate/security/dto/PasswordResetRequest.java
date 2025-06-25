package pl.sumatywny.travelmate.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to reset password with token")
public class PasswordResetRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset token received via email", example = "abc123-def456-ghi789")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "New password", example = "newSecurePassword123")
    private String newPassword;
}