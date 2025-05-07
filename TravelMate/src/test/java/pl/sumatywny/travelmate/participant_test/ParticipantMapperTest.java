package pl.sumatywny.travelmate.participant_test;

import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.service.ParticipantMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ParticipantMapperTest {

    private final ParticipantMapper mapper = new ParticipantMapper();

    @Test
    void toEntity_WithValidDTO_ShouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        ParticipantDTO dto = ParticipantDTO.builder()
                .id(id)
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .email(email)
                .build();

        // When
        Participant entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTripId()).isEqualTo(tripId);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getRole()).isEqualTo(ParticipantRole.MEMBER);
        assertThat(entity.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(entity.getEmail()).isEqualTo(email);
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        // When
        Participant entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void toDTO_WithValidEntity_ShouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        Participant entity = Participant.builder()
                .id(id)
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .email(email)
                .build();

        // When
        ParticipantDTO dto = mapper.toDTO(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTripId()).isEqualTo(tripId);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getRole()).isEqualTo(ParticipantRole.ORGANIZER);
        assertThat(dto.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(dto.getEmail()).isEqualTo(email);
    }

    @Test
    void toDTO_WithNullEntity_ShouldReturnNull() {
        // When
        ParticipantDTO dto = mapper.toDTO(null);

        // Then
        assertThat(dto).isNull();
    }
}