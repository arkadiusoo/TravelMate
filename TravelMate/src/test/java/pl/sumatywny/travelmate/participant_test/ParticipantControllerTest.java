package pl.sumatywny.travelmate.participant_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.participant.controller.ParticipantController;
import pl.sumatywny.travelmate.participant.dto.InvitationResponseDTO;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.service.ParticipantService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParticipantController.class)
public class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipantService participantService;

    private UUID tripId;
    private UUID participantId;
    private UUID currentUserId; // The mock current user ID
    private ParticipantDTO participantDTO;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        // This must match the value in ParticipantController.getCurrentUserId()
        currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        participantDTO = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(UUID.randomUUID())
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .email("test@example.com")
                .build();
    }

    @Test
    void getParticipants_ReturnsListOfParticipants() throws Exception {
        // Given
        List<ParticipantDTO> participants = Arrays.asList(participantDTO);
        when(participantService.getParticipantsByTrip(tripId)).thenReturn(participants);

        // When/Then
        mockMvc.perform(get("/trips/{tripId}/participants", tripId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(participantId.toString())));
    }

    @Test
    void inviteParticipant_ReturnsCreatedParticipant() throws Exception {
        // Given
        ParticipantDTO inputDTO = ParticipantDTO.builder()
                .tripId(tripId)  // Add this line to set tripId
                .userId(UUID.randomUUID())
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantService.addParticipant(any(ParticipantDTO.class), eq(currentUserId)))
                .thenReturn(participantDTO);

        // When/Then
        mockMvc.perform(post("/trips/{tripId}/participants", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(participantId.toString())));
    }

    @Test
    void updateParticipant_ReturnsUpdatedParticipant() throws Exception {
        // Given
        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .tripId(tripId)  // Add this line
                .role(ParticipantRole.ORGANIZER)
                .build();

        ParticipantDTO updatedDTO = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(participantDTO.getUserId())
                .role(ParticipantRole.ORGANIZER) // Updated role
                .status(participantDTO.getStatus())
                .email(participantDTO.getEmail())
                .build();

        when(participantService.updateParticipantRole(eq(participantId), any(ParticipantDTO.class), eq(currentUserId)))
                .thenReturn(updatedDTO);

        // When/Then
        mockMvc.perform(put("/trips/{tripId}/participants/{participantId}", tripId, participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ORGANIZER")));
    }

    @Test
    void removeParticipant_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(participantService).removeParticipant(participantId, currentUserId);

        // When/Then
        mockMvc.perform(delete("/trips/{tripId}/participants/{participantId}", tripId, participantId))
                .andExpect(status().isNoContent());
    }

    @Test
    void respondToInvitation_ReturnsUpdatedParticipant() throws Exception {
        // Given
        InvitationResponseDTO responseDTO = new InvitationResponseDTO(InvitationStatus.ACCEPTED);

        ParticipantDTO updatedDTO = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(participantDTO.getUserId())
                .role(participantDTO.getRole())
                .status(InvitationStatus.ACCEPTED) // Updated status
                .email(participantDTO.getEmail())
                .build();

        // THIS WAS THE ISSUE - We need to pass the currentUserId as the third parameter
        when(participantService.respondToInvitation(
                eq(participantId),
                eq(InvitationStatus.ACCEPTED),
                eq(currentUserId)))
                .thenReturn(updatedDTO);

        // When/Then
        mockMvc.perform(patch("/trips/{tripId}/participants/{participantId}/respond", tripId, participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    void respondToInvitation_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJson = "{ \"status\": null }";

        // When/Then
        mockMvc.perform(patch("/trips/{tripId}/participants/{participantId}/respond", tripId, participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}