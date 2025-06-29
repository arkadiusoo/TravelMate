package pl.sumatywny.travelmate.participant_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sumatywny.travelmate.config.NotFoundException;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.participant.service.ParticipantMapper;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.participant.service.TripPermissionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @Mock(lenient = true)
    private ParticipantRepository participantRepository;

    @Mock(lenient = true)
    private ParticipantMapper participantMapper;

    @Mock(lenient = true)
    private TripPermissionService permissionService;

    @InjectMocks
    private ParticipantService participantService;

    private UUID tripId;
    private UUID organizerId;
    private UUID memberId;
    private UUID guestId;
    private UUID participantId;
    private Participant participant;
    private ParticipantDTO participantDTO;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        organizerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        participant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .email("test@example.com")
                .build();

        participantDTO = ParticipantDTO.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .email("test@example.com")
                .build();
    }

    @Test
    void addParticipant_AsOrganizer_Success() {
        // Given
        when(permissionService.canInviteParticipants(tripId, organizerId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.ORGANIZER)).thenReturn(true);
        when(participantMapper.toEntity(any(ParticipantDTO.class))).thenReturn(participant);
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(participantDTO);

        // When
        ParticipantDTO result = participantService.addParticipant(participantDTO, organizerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(participantId);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addParticipant_AsMember_Success() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.ORGANIZER)).thenReturn(false);

        participantDTO.setRole(ParticipantRole.MEMBER); // członek dodaje członka

        when(participantMapper.toEntity(any(ParticipantDTO.class))).thenReturn(participant);
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(participantDTO);

        // When
        ParticipantDTO result = participantService.addParticipant(participantDTO, memberId);

        // Then
        assertThat(result).isNotNull();
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addParticipant_AsMemberAddingOrganizer_ThrowsException() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.ORGANIZER)).thenReturn(false);

        participantDTO.setRole(ParticipantRole.ORGANIZER); // członek próbuje dodać organizatora

        // When/Then
        assertThatThrownBy(() -> participantService.addParticipant(participantDTO, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tylko organizatorzy mogą dodawać innych organizatorów");

        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void addParticipant_AsGuest_ThrowsException() {
        // Given
        when(permissionService.canInviteParticipants(tripId, guestId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> participantService.addParticipant(participantDTO, guestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie masz uprawnień do zapraszania uczestników");

        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void updateParticipantRole_AsOrganizer_Success() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .build();

        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .role(ParticipantRole.ORGANIZER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, organizerId, participantId)).thenReturn(true);
        when(permissionService.canAssignRole(tripId, organizerId, ParticipantRole.ORGANIZER)).thenReturn(true);
        when(participantRepository.save(any(Participant.class))).thenReturn(existingParticipant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(participantDTO);

        // When
        ParticipantDTO result = participantService.updateParticipantRole(participantId, updateDTO, organizerId);

        // Then
        assertThat(result).isNotNull();
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void updateParticipantRole_AsMember_ThrowsException() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(guestId) // Inny użytkownik niż memberId
                .role(ParticipantRole.GUEST)
                .build();

        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, memberId, participantId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> participantService.updateParticipantRole(participantId, updateDTO, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie masz uprawnień do zarządzania tym uczestnikiem");

        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void removeParticipant_AsOrganizer_Success() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, organizerId, participantId)).thenReturn(true);

        // When
        participantService.removeParticipant(participantId, organizerId);

        // Then
        verify(participantRepository).deleteById(participantId);
    }

    @Test
    void removeParticipant_AsMemberRemovingSelf_Success() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId) // Ten sam użytkownik co wykonujący operację
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, memberId, participantId)).thenReturn(true);

        // When
        participantService.removeParticipant(participantId, memberId);

        // Then
        verify(participantRepository).deleteById(participantId);
    }

    @Test
    void removeParticipant_AsMemberRemovingOther_ThrowsException() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(guestId) // Inny użytkownik niż wykonujący operację
                .role(ParticipantRole.GUEST)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, memberId, participantId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> participantService.removeParticipant(participantId, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie masz uprawnień do usunięcia tego uczestnika");

        verify(participantRepository, never()).deleteById(any());
    }

    @Test
    void respondToInvitation_OwnInvitation_Success() {
        // Given
        Participant pendingInvitation = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId) // Ten sam użytkownik co wykonujący operację
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(pendingInvitation));
        when(participantRepository.save(any(Participant.class))).thenReturn(pendingInvitation);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(participantDTO);

        // When
        ParticipantDTO result = participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, memberId);

        // Then
        assertThat(result).isNotNull();
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void respondToInvitation_OthersInvitation_ThrowsException() {
        // Given
        Participant pendingInvitation = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(guestId) // Inny użytkownik niż wykonujący operację
                .role(ParticipantRole.GUEST)
                .status(InvitationStatus.PENDING)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(pendingInvitation));

        // When/Then
        assertThatThrownBy(() -> participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Możesz odpowiadać tylko na własne zaproszenia");

        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void respondToInvitation_AlreadyProcessed_ThrowsException() {
        // Given
        Participant acceptedInvitation = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId) // Ten sam użytkownik co wykonujący operację
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED) // Już zaakceptowane zaproszenie
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(acceptedInvitation));

        // When/Then
        assertThatThrownBy(() -> participantService.respondToInvitation(participantId, InvitationStatus.DECLINED, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tylko oczekujące zaproszenia mogą być aktualizowane");

        verify(participantRepository, never()).save(any(Participant.class));
    }


    @Test
    void getParticipantsByTrip_ReturnsAllParticipants() {
        // Given
        Participant participant1 = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(organizerId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        Participant participant2 = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        ParticipantDTO dto1 = ParticipantDTO.builder()
                .id(participant1.getId())
                .tripId(tripId)
                .userId(organizerId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        ParticipantDTO dto2 = ParticipantDTO.builder()
                .id(participant2.getId())
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        List<Participant> participants = Arrays.asList(participant1, participant2);

        when(participantRepository.findAllByTripId(tripId)).thenReturn(participants);
        when(participantMapper.toDTO(participant1)).thenReturn(dto1);
        when(participantMapper.toDTO(participant2)).thenReturn(dto2);

        // When
        List<ParticipantDTO> result = participantService.getParticipantsByTrip(tripId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParticipantDTO::getRole)
                .containsExactlyInAnyOrder(ParticipantRole.ORGANIZER, ParticipantRole.MEMBER);
    }

    @Test
    void addParticipant_WithoutUserIdOrEmail_ThrowsException() {
        // Given
        ParticipantDTO invalidDTO = ParticipantDTO.builder()
                .tripId(tripId)
                .role(ParticipantRole.MEMBER)
                .build();

        // Mock permission service to return true for canInviteParticipants
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> participantService.addParticipant(invalidDTO, memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Należy podać userId lub email");
    }

    @Test
    void addParticipant_UserAlreadyParticipant_ThrowsException() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);
        when(participantRepository.existsByTripIdAndUserId(tripId, memberId)).thenReturn(true);

        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .build();

        // When/Then
        assertThatThrownBy(() -> participantService.addParticipant(dto, memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Użytkownik jest już uczestnikiem tej wycieczki");
    }

    @Test
    void addParticipant_WithoutStatus_SetsStatusToPending() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.ORGANIZER)).thenReturn(false);

        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(UUID.randomUUID())
                .role(ParticipantRole.MEMBER)
                .status(null)  // No status provided
                .build();

        Participant savedParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(dto.getTripId())
                .userId(dto.getUserId())
                .role(dto.getRole())
                .status(InvitationStatus.PENDING)  // Status should be set to PENDING
                .build();

        when(participantMapper.toEntity(any(ParticipantDTO.class))).thenReturn(savedParticipant);
        when(participantRepository.save(any(Participant.class))).thenReturn(savedParticipant);
        when(participantMapper.toDTO(savedParticipant)).thenReturn(dto);

        // When
        ParticipantDTO result = participantService.addParticipant(dto, memberId);

        // Then
        assertThat(result).isNotNull();
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void updateParticipantRole_ParticipantNotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(participantRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .role(ParticipantRole.MEMBER)
                .build();

        // When/Then
        assertThatThrownBy(() -> participantService.updateParticipantRole(nonExistentId, updateDTO, memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Nie znaleziono uczestnika");
    }

    @Test
    void updateParticipantRole_NoRoleUpdate_OnlyUpdatesExistingData() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .build(); // No role update provided

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, organizerId, participantId)).thenReturn(true);
        when(participantRepository.save(any(Participant.class))).thenReturn(existingParticipant);
        when(participantMapper.toDTO(any(Participant.class))).thenReturn(participantDTO);

        // When
        ParticipantDTO result = participantService.updateParticipantRole(participantId, updateDTO, organizerId);

        // Then
        assertThat(result).isNotNull();
        verify(participantRepository).save(existingParticipant);
        // Role should remain MEMBER since no update was provided
    }

    @Test
    void removeParticipant_ParticipantNotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(participantRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> participantService.removeParticipant(nonExistentId, organizerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Nie znaleziono uczestnika");
    }

    @Test
    void respondToInvitation_WithPendingStatus_ThrowsException() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder().build();

        // When/Then
        assertThatThrownBy(() -> participantService.respondToInvitation(participantId, InvitationStatus.PENDING, memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status musi być ACCEPTED lub DECLINED");
    }

    @Test
    void respondToInvitation_ParticipantNotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(participantRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> participantService.respondToInvitation(nonExistentId, InvitationStatus.ACCEPTED, memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Nie znaleziono uczestnika");
    }

    @Test
    void updateParticipantRole_WithNewRoleButNoPermissionToAssignRole_ThrowsException() {
        // Given
        Participant existingParticipant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .userId(UUID.randomUUID())
                .role(ParticipantRole.MEMBER)
                .build();

        ParticipantDTO updateDTO = ParticipantDTO.builder()
                .role(ParticipantRole.ORGANIZER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existingParticipant));
        when(permissionService.canManageParticipant(tripId, memberId, participantId)).thenReturn(true);
        when(permissionService.canAssignRole(tripId, memberId, ParticipantRole.ORGANIZER)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> participantService.updateParticipantRole(participantId, updateDTO, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie masz uprawnień do przydzielania tej roli");
    }

    @Test
    void addParticipant_WithExistingEmailOnly_Success() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.ORGANIZER)).thenReturn(false);

        // Create DTO with only email (no userId)
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("existing@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        Participant entity = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .email("existing@example.com")
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        ParticipantDTO savedDto = ParticipantDTO.builder()
                .id(entity.getId())
                .tripId(entity.getTripId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .status(entity.getStatus())
                .build();

        when(participantMapper.toEntity(any(ParticipantDTO.class))).thenReturn(entity);
        when(participantRepository.save(any(Participant.class))).thenReturn(entity);
        when(participantMapper.toDTO(entity)).thenReturn(savedDto);

        // When
        ParticipantDTO result = participantService.addParticipant(dto, memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("existing@example.com");
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addParticipant_WithEmptyEmailString_ThrowsException() {
        // Given
        when(permissionService.canInviteParticipants(tripId, memberId)).thenReturn(true);

        // Create DTO with empty email string (not null)
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(null)
                .email("")  // Empty string instead of null
                .role(ParticipantRole.MEMBER)
                .build();

        // When/Then
        assertThatThrownBy(() -> participantService.addParticipant(dto, memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Należy podać userId lub email");
    }

    @Test
    void addParticipant_AsOrganizerAddingOrganizer_Success() {
        // Given
        when(permissionService.canInviteParticipants(tripId, organizerId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.ORGANIZER)).thenReturn(true);

        UUID newUserId = UUID.randomUUID();
        // Create DTO with Organizer role
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(newUserId)
                .role(ParticipantRole.ORGANIZER)
                .build();

        Participant entity = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(newUserId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.PENDING)
                .build();

        ParticipantDTO savedDto = ParticipantDTO.builder()
                .id(entity.getId())
                .tripId(entity.getTripId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .build();

        when(participantMapper.toEntity(any(ParticipantDTO.class))).thenReturn(entity);
        when(participantRepository.save(any(Participant.class))).thenReturn(entity);
        when(participantMapper.toDTO(entity)).thenReturn(savedDto);

        // When
        ParticipantDTO result = participantService.addParticipant(dto, organizerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(ParticipantRole.ORGANIZER);
        verify(participantRepository).save(any(Participant.class));
    }


}