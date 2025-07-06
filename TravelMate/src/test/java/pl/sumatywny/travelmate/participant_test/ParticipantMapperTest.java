package pl.sumatywny.travelmate.participant_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.service.ParticipantMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantMapperTest {

    private ParticipantMapper participantMapper;
    private UUID participantId;
    private UUID tripId;
    private UUID userId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        participantMapper = new ParticipantMapper();
        participantId = UUID.randomUUID();
        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();
    }

    @Test
    void toEntity_ShouldConvertDtoToEntity_WhenValidDto() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .createdAt(now)
                .joinedAt(now)
                .updatedAt(now)
                .build();

        // When
        Participant entity = participantMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getTripId(), entity.getTripId());
        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getRole(), entity.getRole());
        assertEquals(dto.getStatus(), entity.getStatus());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDtoIsNull() {
        // Given
        ParticipantDTO dto = null;

        // When
        Participant entity = participantMapper.toEntity(dto);

        // Then
        assertNull(entity);
    }

    @Test
    void toEntity_ShouldHandleNullFields_WhenDtoHasNullValues() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        // When
        Participant entity = participantMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getEmail());
        assertEquals(dto.getTripId(), entity.getTripId());
        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getRole(), entity.getRole());
        assertEquals(dto.getStatus(), entity.getStatus());
    }

    @Test
    void toDTO_ShouldConvertEntityToDto_WhenValidEntity() {
        // Given
        Participant entity = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(userId)
                .email("test@example.com")
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .createdAt(now)
                .joinedAt(now)
                .updatedAt(now)
                .build();

        // When
        ParticipantDTO dto = participantMapper.toDTO(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getTripId(), dto.getTripId());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.getRole(), dto.getRole());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(entity.getJoinedAt(), dto.getJoinedAt());
        assertEquals(entity.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void toDTO_ShouldReturnNull_WhenEntityIsNull() {
        // Given
        Participant entity = null;

        // When
        ParticipantDTO dto = participantMapper.toDTO(entity);

        // Then
        assertNull(dto);
    }

    @Test
    void toDTO_ShouldHandleNullFields_WhenEntityHasNullValues() {
        // Given
        Participant entity = Participant.builder()
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.GUEST)
                .status(InvitationStatus.DECLINED)
                .createdAt(now)
                .build();

        // When
        ParticipantDTO dto = participantMapper.toDTO(entity);

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getEmail());
        assertNull(dto.getJoinedAt());
        assertNull(dto.getUpdatedAt());
        assertEquals(entity.getTripId(), dto.getTripId());
        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getRole(), dto.getRole());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void toDTO_ShouldMapAllRoles_WhenDifferentRoles() {
        // Given
        Participant organizerEntity = Participant.builder()
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        Participant memberEntity = Participant.builder()
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        Participant guestEntity = Participant.builder()
                .role(ParticipantRole.GUEST)
                .status(InvitationStatus.DECLINED)
                .build();

        // When
        ParticipantDTO organizerDto = participantMapper.toDTO(organizerEntity);
        ParticipantDTO memberDto = participantMapper.toDTO(memberEntity);
        ParticipantDTO guestDto = participantMapper.toDTO(guestEntity);

        // Then
        assertEquals(ParticipantRole.ORGANIZER, organizerDto.getRole());
        assertEquals(ParticipantRole.MEMBER, memberDto.getRole());
        assertEquals(ParticipantRole.GUEST, guestDto.getRole());
    }

    @Test
    void toDTO_ShouldMapAllStatuses_WhenDifferentStatuses() {
        // Given
        Participant pendingEntity = Participant.builder()
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        Participant acceptedEntity = Participant.builder()
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        Participant declinedEntity = Participant.builder()
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.DECLINED)
                .build();

        // When
        ParticipantDTO pendingDto = participantMapper.toDTO(pendingEntity);
        ParticipantDTO acceptedDto = participantMapper.toDTO(acceptedEntity);
        ParticipantDTO declinedDto = participantMapper.toDTO(declinedEntity);

        // Then
        assertEquals(InvitationStatus.PENDING, pendingDto.getStatus());
        assertEquals(InvitationStatus.ACCEPTED, acceptedDto.getStatus());
        assertEquals(InvitationStatus.DECLINED, declinedDto.getStatus());
    }

    @Test
    void roundTrip_ShouldPreserveData_WhenConvertingDtoToEntityAndBack() {
        // Given
        ParticipantDTO originalDto = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(userId)
                .email("roundtrip@example.com")
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        // When
        Participant entity = participantMapper.toEntity(originalDto);
        ParticipantDTO resultDto = participantMapper.toDTO(entity);

        // Then
        assertEquals(originalDto.getId(), resultDto.getId());
        assertEquals(originalDto.getTripId(), resultDto.getTripId());
        assertEquals(originalDto.getUserId(), resultDto.getUserId());
        assertEquals(originalDto.getEmail(), resultDto.getEmail());
        assertEquals(originalDto.getRole(), resultDto.getRole());
        assertEquals(originalDto.getStatus(), resultDto.getStatus());
    }
}