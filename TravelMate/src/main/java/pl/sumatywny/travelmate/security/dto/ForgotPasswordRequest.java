package pl.sumatywny.travelmate.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to initiate password reset")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    @Schema(description = "Email address of the user requesting password reset", example = "user@example.com")
    private String email;
}