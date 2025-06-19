package pl.sumatywny.travelmate.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.security.dto.*;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.AuthService;
import pl.sumatywny.travelmate.security.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and password reset operations")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user and returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Get current user",
            description = "Returns the currently authenticated user's information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user information retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @Operation(
            summary = "Initiate password reset",
            description = "Sends password reset link to user's email if account exists"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent (if account exists)"),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address for password reset",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Forgot password request",
                                    value = """
                                            {
                                              "email": "user@example.com"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.initiateForgotPassword(request));
    }

    @Operation(
            summary = "Reset password",
            description = "Resets user password using the token received via email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password reset token and new password",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Password reset request",
                                    value = """
                                            {
                                              "token": "abc123-def456-ghi789",
                                              "newPassword": "newSecurePassword123"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }

    @Operation(
            summary = "Validate reset token",
            description = "Checks if a password reset token is valid and not expired"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validation result"),
            @ApiResponse(responseCode = "400", description = "Token is required", content = @Content)
    })
    @GetMapping("/validate-reset-token")
    public ResponseEntity<PasswordResetResponse> validateResetToken(
            @RequestParam String token) {
        boolean isValid = passwordResetService.isTokenValid(token);

        return ResponseEntity.ok(PasswordResetResponse.builder()
                .success(isValid)
                .message(isValid ? "Token is valid" : "Token is invalid or expired")
                .build());
    }
}