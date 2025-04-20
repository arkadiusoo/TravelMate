package pl.sumatywny.travelmate.participant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.service.ParticipantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/participants")
@RequiredArgsConstructor
@Tag(name = "Participants", description = "Operations for managing trip participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(
            summary = "Get all participants for a trip",
            description = "Returns a list of all participants in a given trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of participants retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ParticipantDTO>> getParticipants(
            @Parameter(description = "ID of the trip", required = true)
            @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(participantService.getParticipantsByTrip(tripId));
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
            @Parameter(description = "ID of the trip", required = true)
            @PathVariable UUID tripId,
            @Valid @RequestBody ParticipantDTO participantDTO
    ) {
        participantDTO.setTripId(tripId);
        return ResponseEntity.ok(participantService.addParticipant(participantDTO));
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
            @Parameter(description = "ID of the trip", required = true)
            @PathVariable UUID tripId,
            @Parameter(description = "ID of the participant to update", required = true)
            @PathVariable UUID participantId,
            @Valid @RequestBody ParticipantDTO updates
    ) {
        return ResponseEntity.ok(participantService.updateParticipantRole(participantId, updates));
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
            @Parameter(description = "ID of the trip", required = true)
            @PathVariable UUID tripId,
            @Parameter(description = "ID of the participant to remove", required = true)
            @PathVariable UUID participantId
    ) {
        participantService.removeParticipant(participantId);
        return ResponseEntity.noContent().build();
    }
}