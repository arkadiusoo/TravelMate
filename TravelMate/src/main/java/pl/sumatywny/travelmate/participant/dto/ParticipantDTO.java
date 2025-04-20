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

import java.util.UUID;

/**
 * Data Transfer Object for Participant entity.
 * Used for communication between client and server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing a trip participant")
public class ParticipantDTO {

    @Schema(description = "Unique identifier of the participant", example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @Schema(description = "ID of the trip this participant is associated with", example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3")
    private UUID tripId;

    @Schema(description = "ID of the user who is the participant", example = "e8c40d9a-11e2-47cb-90fc-1c6d5bd6b0ae")
    private UUID userId;

    @Email
    @Schema(description = "Email address for inviting users who don't have an account yet", example = "user@example.com")
    private String email;

    @NotNull
    @Schema(description = "Role of the participant in the trip", example = "MEMBER")
    private ParticipantRole role;

    @Schema(description = "Current status of the invitation", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private InvitationStatus status;
}