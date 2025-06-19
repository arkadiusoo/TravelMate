package pl.sumatywny.travelmate.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for password reset operations")
public class PasswordResetResponse {

    @Schema(description = "Success message", example = "Password reset link sent to your email")
    private String message;

    @Schema(description = "Whether the operation was successful", example = "true")
    private boolean success;
}