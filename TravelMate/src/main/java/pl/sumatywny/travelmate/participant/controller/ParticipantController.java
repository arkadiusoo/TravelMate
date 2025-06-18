package pl.sumatywny.travelmate.participant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.participant.dto.InvitationResponseDTO;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.AuthService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/trips/{tripId}/participants")
@RequiredArgsConstructor
@Tag(name = "Participants", description = "Operations for managing trip participants")
public class ParticipantController {

    private final ParticipantService participantService;
    @Autowired
    private AuthService authService;

    /**
     * Pobiera identyfikator aktualnie zalogowanego użytkownika.
     *
     * @return ID bieżącego użytkownika
     */
    private UUID getCurrentUserId() {
        return authService.getCurrentUserId();
    }

    @Operation(
            summary = "Get all participants for a trip",
            description = "Returns a list of all participants in a given trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of participants retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - not a participant"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ParticipantDTO>> getParticipants(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId
    ) {
        UUID currentUserId = getCurrentUserId();

        // Check if current user is a participant in this trip
        List<ParticipantDTO> allParticipants = participantService.getParticipantsByTrip(tripId);
        boolean isParticipant = allParticipants.stream()
                .anyMatch(p -> p.getUserId().equals(currentUserId));

        if (!isParticipant) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return ResponseEntity.ok(allParticipants);
    }

    @Operation(
            summary = "Invite a new participant to a trip",
            description = "Adds a new participant to a trip by user ID or email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant invited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ParticipantDTO> inviteParticipant(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Participant invitation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParticipantDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invite by User ID",
                                            description = "Inviting an existing user by their ID",
                                            value = """
                                                    {
                                                      "userId": "e8c40d9a-11e2-47cb-90fc-1c6d5bd6b0ae",
                                                      "role": "MEMBER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invite by Email",
                                            description = "Inviting an existing registered user by their email address",
                                            value = """
                                                    {
                                                      "email": "john.doe@example.com",
                                                      "role": "MEMBER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invite Organizer",
                                            description = "Inviting someone as an organizer",
                                            value = """
                                                    {
                                                      "email": "organizer@example.com",
                                                      "role": "ORGANIZER"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ParticipantDTO participantDTO
    ) {
        participantDTO.setTripId(tripId);
        return ResponseEntity.ok(participantService.addParticipant(participantDTO, getCurrentUserId()));
    }

    @Operation(
            summary = "Update a participant's role",
            description = "Updates the role of a participant in a trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{participantId}")
    public ResponseEntity<ParticipantDTO> updateParticipant(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "ID of the participant to update",
                    required = true,
                    example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f"
            )
            @PathVariable UUID participantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updates to apply to the participant",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Promote to Organizer",
                                            value = """
                                                    {
                                                      "role": "ORGANIZER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Demote to Member",
                                            value = """
                                                    {
                                                      "role": "MEMBER"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ParticipantDTO updates
    ) {
        return ResponseEntity.ok(participantService.updateParticipantRole(participantId, updates, getCurrentUserId()));
    }

    @Operation(
            summary = "Remove a participant from a trip",
            description = "Removes a participant from a trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Participant removed successfully"),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "ID of the participant to remove",
                    required = true,
                    example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f"
            )
            @PathVariable UUID participantId
    ) {
        participantService.removeParticipant(participantId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Respond to a trip invitation",
            description = "Allows a user to accept or decline an invitation to a trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation response processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "409", description = "Invitation has already been processed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{participantId}/respond")
    public ResponseEntity<ParticipantDTO> respondToInvitation(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "ID of the invitation to respond to",
                    required = true,
                    example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f"
            )
            @PathVariable UUID participantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Response to the invitation",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Accept Invitation",
                                            value = """
                                                    {
                                                      "status": "ACCEPTED"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Decline Invitation",
                                            value = """
                                                    {
                                                      "status": "DECLINED"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody InvitationResponseDTO response
    ) {
        return ResponseEntity.ok(participantService.respondToInvitation(
                participantId, response.getStatus(), getCurrentUserId()));
    }

    @Operation(
            summary = "Update a participant's role by email",
            description = "Updates the role of a participant in a trip using their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/email/{email}")
    public ResponseEntity<ParticipantDTO> updateParticipantByEmail(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "Email of the participant to update",
                    required = true,
                    example = "john.doe@example.com"
            )
            @PathVariable String email,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updates to apply to the participant",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Promote to Organizer",
                                            value = """
                                                    {
                                                      "role": "ORGANIZER"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Change to Member",
                                            value = """
                                                    {
                                                      "role": "MEMBER"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ParticipantDTO updates
    ) {
        return ResponseEntity.ok(participantService.updateParticipantRoleByEmail(tripId, email, updates, getCurrentUserId()));
    }

    @Operation(
            summary = "Remove a participant from a trip by email",
            description = "Removes a participant from a trip using their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Participant removed successfully"),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> removeParticipantByEmail(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "Email of the participant to remove",
                    required = true,
                    example = "john.doe@example.com"
            )
            @PathVariable String email
    ) {
        participantService.removeParticipantByEmail(tripId, email, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Respond to a trip invitation by email",
            description = "Allows a user to accept or decline an invitation to a trip using their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation response processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Participant not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "409", description = "Invitation has already been processed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/email/{email}/respond")
    public ResponseEntity<ParticipantDTO> respondToInvitationByEmail(
            @Parameter(
                    description = "ID of the trip",
                    required = true,
                    example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3"
            )
            @PathVariable UUID tripId,
            @Parameter(
                    description = "Email of the participant responding to invitation",
                    required = true,
                    example = "john.doe@example.com"
            )
            @PathVariable String email,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Response to the invitation",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Accept Invitation",
                                            value = """
                                                    {
                                                      "status": "ACCEPTED"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Decline Invitation",
                                            value = """
                                                    {
                                                      "status": "DECLINED"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody InvitationResponseDTO response
    ) {
        return ResponseEntity.ok(participantService.respondToInvitationByEmail(
                tripId, email, response.getStatus(), getCurrentUserId()));
    }
}