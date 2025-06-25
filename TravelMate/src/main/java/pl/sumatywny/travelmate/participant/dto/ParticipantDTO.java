package pl.sumatywny.travelmate.participant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Participant entity.
 * Used for communication between client and server.
 * Supports inviting existing registered users by either userId or email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing a trip participant")
public class ParticipantDTO {

    @Schema(
            description = "Unique identifier of the participant record",
            example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "ID of the trip this participant is associated with",
            example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3",
            accessMode = Schema.AccessMode.READ_ONLY  // Since it's set by controller
    )
    private UUID tripId;

    @Schema(
            description = "ID of the registered user to invite as participant. Either userId or email must be provided when creating a participant.",
            example = "e8c40d9a-11e2-47cb-90fc-1c6d5bd6b0ae"
    )
    private UUID userId;

    @Email
    @Schema(
            description = "Email address of the registered user to invite as participant. Either userId or email must be provided when creating a participant.",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "First name of the user (populated from User entity)",
            example = "John",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String firstName;

    @Schema(
            description = "Last name of the user (populated from User entity)",
            example = "Doe",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String lastName;

    @NotNull
    @Schema(
            description = "Role of the participant in the trip",
            example = "MEMBER",
            allowableValues = {"ORGANIZER", "MEMBER", "GUEST"}
    )
    private ParticipantRole role;

    @Schema(
            description = "Current status of the invitation",
            example = "PENDING",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"PENDING", "ACCEPTED", "DECLINED"}
    )
    private InvitationStatus status;

    @Schema(
            description = "When the invitation was created",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "When the user joined (accepted the invitation)",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime joinedAt;

    @Schema(
            description = "When the participant record was last updated",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    /**
     * Helper method to get display name for UI
     */
    public String getDisplayName() {
        if (firstName != null && lastName != null && !firstName.trim().isEmpty() && !lastName.trim().isEmpty()) {
            return (firstName + " " + lastName).trim();
        }

        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName;
        }

        if (email != null) {
            return email.split("@")[0];
        }

        return "Unknown User";
    }
}