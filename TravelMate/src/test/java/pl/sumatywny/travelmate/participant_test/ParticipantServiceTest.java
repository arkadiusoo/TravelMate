package pl.sumatywny.travelmate.participant_test;

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
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private ParticipantMapper participantMapper;
    @Mock
    private TripPermissionService permissionService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ParticipantService participantService;

    private final UUID tripId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID currentUserId = UUID.randomUUID();
    private final UUID participantId = UUID.randomUUID();

    @Test
    void addParticipant_ShouldThrowException_WhenNoPermission() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertEquals("Nie masz uprawnień do zapraszania uczestników", exception.getMessage());
    }

    @Test
    void addParticipant_ShouldThrowException_WhenUserNotFound() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("notfound@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("notfound@example.com")).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertTrue(exception.getMessage().contains("Nie znaleziono zarejestrowanego użytkownika"));
    }

    @Test
    void addParticipant_ShouldThrowException_WhenUserAlreadyAccepted() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        Participant existingParticipant = Participant.builder()
                .userId(userId)
                .status(InvitationStatus.ACCEPTED)
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("test@example.com")).thenReturn(userId);
        when(participantRepository.findByTripIdAndUserId(tripId, userId)).thenReturn(Optional.of(existingParticipant));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertEquals("Użytkownik jest już uczestnikiem tej wycieczki", exception.getMessage());
    }

    @Test
    void addParticipant_ShouldReInviteDeclinedUser() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        Participant declinedParticipant = Participant.builder()
                .userId(userId)
                .status(InvitationStatus.DECLINED)
                .role(ParticipantRole.GUEST)
                .build();

        ParticipantDTO returnedDto = ParticipantDTO.builder().email("test@example.com").build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("test@example.com")).thenReturn(userId);
        when(participantRepository.findByTripIdAndUserId(tripId, userId)).thenReturn(Optional.of(declinedParticipant));
        when(participantRepository.save(declinedParticipant)).thenReturn(declinedParticipant);
        when(participantMapper.toDTO(declinedParticipant)).thenReturn(returnedDto);

        // When
        ParticipantDTO result = participantService.addParticipant(dto, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(ParticipantRole.MEMBER, declinedParticipant.getRole());
        assertEquals(InvitationStatus.PENDING, declinedParticipant.getStatus());
        verify(participantRepository).save(declinedParticipant);
    }

    @Test
    void updateParticipantRole_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.updateParticipantRole(participantId, updates, currentUserId));
    }

    @Test
    void updateParticipantRole_ShouldThrowException_WhenNoPermission() {
        // Given
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();
        Participant existing = Participant.builder().tripId(tripId).build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existing));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.updateParticipantRole(participantId, updates, currentUserId));

        assertEquals("Nie masz uprawnień do zarządzania tym uczestnikiem", exception.getMessage());
    }

    @Test
    void removeParticipant_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.removeParticipant(participantId, currentUserId));
    }

    @Test
    void removeParticipant_ShouldDeleteParticipant_WhenValidInput() {
        // Given
        Participant participant = Participant.builder().tripId(tripId).build();
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(true);

        // When
        participantService.removeParticipant(participantId, currentUserId);

        // Then
        verify(participantRepository).deleteById(participantId);
    }

    @Test
    void getParticipantsByTrip_ShouldReturnParticipantsWithUserDetails() {
        // Given
        Participant participant = Participant.builder()
                .userId(userId)
                .email("old@example.com")
                .build();

        ParticipantDTO dto = ParticipantDTO.builder()
                .email("old@example.com")
                .build();

        User user = User.builder()
                .email("new@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(participant));
        when(participantMapper.toDTO(participant)).thenReturn(dto);
        when(userService.findById(userId)).thenReturn(user);

        // When
        List<ParticipantDTO> result = participantService.getParticipantsByTrip(tripId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("new@example.com", dto.getEmail()); // Should be updated from User
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
    }

    @Test
    void respondToInvitation_ShouldThrowException_WhenInvalidStatus() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.respondToInvitation(participantId, InvitationStatus.PENDING, currentUserId));

        assertEquals("Status musi być ACCEPTED lub DECLINED", exception.getMessage());
    }

    @Test
    void respondToInvitation_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, currentUserId));
    }

    @Test
    void respondToInvitation_ShouldThrowException_WhenNotPendingStatus() {
        // Given
        Participant participant = Participant.builder()
                .status(InvitationStatus.ACCEPTED)
                .userId(currentUserId)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, currentUserId));

        assertEquals("Tylko oczekujące zaproszenia mogą być aktualizowane", exception.getMessage());
    }

    @Test
    void respondToInvitation_ShouldThrowException_WhenNotOwnInvitation() {
        // Given
        Participant participant = Participant.builder()
                .status(InvitationStatus.PENDING)
                .userId(UUID.randomUUID()) // Different user
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, currentUserId));

        assertEquals("Możesz odpowiadać tylko na własne zaproszenia", exception.getMessage());
    }

    @Test
    void respondToInvitation_ShouldAcceptInvitation_WhenValidInput() {
        // Given
        Participant participant = Participant.builder()
                .status(InvitationStatus.PENDING)
                .userId(currentUserId)
                .build();

        ParticipantDTO dto = ParticipantDTO.builder().build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);
        when(participantMapper.toDTO(participant)).thenReturn(dto);

        // When
        ParticipantDTO result = participantService.respondToInvitation(participantId, InvitationStatus.ACCEPTED, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(InvitationStatus.ACCEPTED, participant.getStatus());
        assertNotNull(participant.getJoinedAt());
        verify(participantRepository).save(participant);
    }

    @Test
    void respondToInvitation_ShouldDeclineInvitation_WhenValidInput() {
        // Given
        Participant participant = Participant.builder()
                .status(InvitationStatus.PENDING)
                .userId(currentUserId)
                .build();

        ParticipantDTO dto = ParticipantDTO.builder().build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);
        when(participantMapper.toDTO(participant)).thenReturn(dto);

        // When
        ParticipantDTO result = participantService.respondToInvitation(participantId, InvitationStatus.DECLINED, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(InvitationStatus.DECLINED, participant.getStatus());
        assertNull(participant.getJoinedAt()); // Should not set joinedAt for declined
        verify(participantRepository).save(participant);
    }

    @Test
    void updateParticipantRoleByEmail_ShouldThrowException_WhenUserNotRegistered() {
        // Given
        String email = "unregistered@example.com";
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();

        when(userService.isRegisteredUser(email)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.updateParticipantRoleByEmail(tripId, email, updates, currentUserId));

        assertEquals("Nie znaleziono zarejestrowanego użytkownika z tym emailem", exception.getMessage());
    }

    @Test
    void removeParticipantByEmail_ShouldThrowException_WhenUserNotRegistered() {
        // Given
        String email = "unregistered@example.com";
        when(userService.isRegisteredUser(email)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.removeParticipantByEmail(tripId, email, currentUserId));

        assertEquals("Nie znaleziono zarejestrowanego użytkownika z tym emailem", exception.getMessage());
    }

    @Test
    void addParticipant_ShouldThrowException_WhenMissingUserIdAndEmail() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .role(ParticipantRole.MEMBER)
                .build(); // No userId or email

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertEquals("Należy podać userId lub email", exception.getMessage());
    }

    // Add these test methods to your existing ParticipantServiceTest.java class

    @Test
    void addParticipant_ShouldCreateNewParticipant_WhenValidInput() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        Participant savedParticipant = Participant.builder()
                .id(participantId)
                .userId(userId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();

        ParticipantDTO returnedDto = ParticipantDTO.builder()
                .id(participantId)
                .email("test@example.com")
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("test@example.com")).thenReturn(userId);
        when(participantRepository.findByTripIdAndUserId(tripId, userId)).thenReturn(Optional.empty());
        when(participantMapper.toEntity(dto)).thenReturn(savedParticipant);
        when(participantRepository.save(savedParticipant)).thenReturn(savedParticipant);
        when(participantMapper.toDTO(savedParticipant)).thenReturn(returnedDto);

        // When
        ParticipantDTO result = participantService.addParticipant(dto, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(InvitationStatus.PENDING, dto.getStatus());
        verify(participantRepository).save(savedParticipant);
    }



    @Test
    void addParticipant_ShouldThrowException_WhenEmailAndUserIdMismatch() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(userId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        UUID differentUserId = UUID.randomUUID();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("test@example.com")).thenReturn(differentUserId); // Different user!

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertTrue(exception.getMessage().contains("nie należą do tego samego użytkownika"));
    }

    @Test
    void addParticipant_ShouldThrowException_WhenUserIdNotFound() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findEmailByUserId(userId)).thenReturn(null); // User not found

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertTrue(exception.getMessage().contains("Nie znaleziono użytkownika z ID"));
    }

    @Test
    void addParticipant_ShouldThrowException_WhenPendingInvitationExists() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.MEMBER)
                .build();

        Participant existingParticipant = Participant.builder()
                .userId(userId)
                .status(InvitationStatus.PENDING)
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(userService.findUserIdByEmail("test@example.com")).thenReturn(userId);
        when(participantRepository.findByTripIdAndUserId(tripId, userId)).thenReturn(Optional.of(existingParticipant));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertEquals("Użytkownik ma już oczekujące zaproszenie do tej wycieczki", exception.getMessage());
    }

    @Test
    void addParticipant_ShouldThrowException_WhenNonOrganizerTriesToAddOrganizer() {
        // Given
        ParticipantDTO dto = ParticipantDTO.builder()
                .tripId(tripId)
                .email("test@example.com")
                .role(ParticipantRole.ORGANIZER) // Trying to add organizer
                .build();

        when(participantRepository.findAllByTripId(tripId)).thenReturn(Arrays.asList(new Participant()));
        when(permissionService.canInviteParticipants(tripId, currentUserId)).thenReturn(true);
        when(permissionService.hasRoleOrHigher(tripId, currentUserId, ParticipantRole.ORGANIZER)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.addParticipant(dto, currentUserId));

        assertEquals("Tylko organizatorzy mogą dodawać innych organizatorów", exception.getMessage());
    }

    @Test
    void updateParticipantRole_ShouldUpdateSuccessfully_WhenValidInput() {
        // Given
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();
        Participant existing = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .role(ParticipantRole.MEMBER)
                .build();

        ParticipantDTO returnedDto = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existing));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(true);
        when(permissionService.canAssignRole(tripId, currentUserId, ParticipantRole.ORGANIZER)).thenReturn(true);
        when(participantRepository.save(existing)).thenReturn(existing);
        when(participantMapper.toDTO(existing)).thenReturn(returnedDto);

        // When
        ParticipantDTO result = participantService.updateParticipantRole(participantId, updates, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(ParticipantRole.ORGANIZER, existing.getRole());
        verify(participantRepository).save(existing);
    }

    @Test
    void updateParticipantRole_ShouldThrowException_WhenCannotAssignRole() {
        // Given
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();
        Participant existing = Participant.builder().tripId(tripId).build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(existing));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(true);
        when(permissionService.canAssignRole(tripId, currentUserId, ParticipantRole.ORGANIZER)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> participantService.updateParticipantRole(participantId, updates, currentUserId));

        assertEquals("Nie masz uprawnień do przydzielania tej roli", exception.getMessage());
    }

    @Test
    void updateParticipantRoleByEmail_ShouldUpdateSuccessfully_WhenValidInput() {
        // Given
        String email = "test@example.com";
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();
        Participant existing = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .build();

        ParticipantDTO returnedDto = ParticipantDTO.builder().build();

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.of(existing));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(true);
        when(permissionService.canAssignRole(tripId, currentUserId, ParticipantRole.ORGANIZER)).thenReturn(true);
        when(participantRepository.save(existing)).thenReturn(existing);
        when(participantMapper.toDTO(existing)).thenReturn(returnedDto);

        // When
        ParticipantDTO result = participantService.updateParticipantRoleByEmail(tripId, email, updates, currentUserId);

        // Then
        assertNotNull(result);
        verify(participantRepository).save(existing);
    }

    @Test
    void updateParticipantRoleByEmail_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        String email = "notfound@example.com";
        ParticipantDTO updates = ParticipantDTO.builder().role(ParticipantRole.ORGANIZER).build();

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.updateParticipantRoleByEmail(tripId, email, updates, currentUserId));
    }

    @Test
    void removeParticipantByEmail_ShouldDeleteSuccessfully_WhenValidInput() {
        // Given
        String email = "test@example.com";
        Participant participant = Participant.builder()
                .id(participantId)
                .tripId(tripId)
                .build();

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.of(participant));
        when(permissionService.canManageParticipant(tripId, currentUserId, participantId)).thenReturn(true);

        // When
        participantService.removeParticipantByEmail(tripId, email, currentUserId);

        // Then
        verify(participantRepository).delete(participant);
    }

    @Test
    void removeParticipantByEmail_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        String email = "notfound@example.com";

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.removeParticipantByEmail(tripId, email, currentUserId));
    }

    @Test
    void respondToInvitationByEmail_ShouldAcceptSuccessfully_WhenValidInput() {
        // Given
        String email = "test@example.com";
        Participant participant = Participant.builder()
                .status(InvitationStatus.PENDING)
                .userId(currentUserId)
                .build();

        ParticipantDTO returnedDto = ParticipantDTO.builder().build();

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);
        when(participantMapper.toDTO(participant)).thenReturn(returnedDto);

        // When
        ParticipantDTO result = participantService.respondToInvitationByEmail(tripId, email, InvitationStatus.ACCEPTED, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(InvitationStatus.ACCEPTED, participant.getStatus());
        assertNotNull(participant.getJoinedAt());
        verify(participantRepository).save(participant);
    }

    @Test
    void respondToInvitationByEmail_ShouldThrowException_WhenUserNotRegistered() {
        // Given
        String email = "unregistered@example.com";

        when(userService.isRegisteredUser(email)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participantService.respondToInvitationByEmail(tripId, email, InvitationStatus.ACCEPTED, currentUserId));

        assertEquals("Nie znaleziono zarejestrowanego użytkownika z tym emailem", exception.getMessage());
    }

    @Test
    void respondToInvitationByEmail_ShouldThrowException_WhenParticipantNotFound() {
        // Given
        String email = "notfound@example.com";

        when(userService.isRegisteredUser(email)).thenReturn(true);
        when(participantRepository.findByTripIdAndEmail(tripId, email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> participantService.respondToInvitationByEmail(tripId, email, InvitationStatus.ACCEPTED, currentUserId));
    }

}