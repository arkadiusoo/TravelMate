package pl.sumatywny.travelmate.participant_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.participant.service.TripPermissionService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPermissionServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private TripPermissionService tripPermissionService;

    private UUID tripId;
    private UUID userId;
    private UUID participantId;
    private Participant organizerParticipant;
    private Participant memberParticipant;
    private Participant guestParticipant;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        organizerParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        memberParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        guestParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .tripId(tripId)
                .userId(userId)
                .role(ParticipantRole.GUEST)
                .status(InvitationStatus.ACCEPTED)
                .build();
    }

    @Test
    void getUserRole_ShouldReturnRole_WhenParticipantExists() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        ParticipantRole result = tripPermissionService.getUserRole(tripId, userId);

        // Then
        assertEquals(ParticipantRole.ORGANIZER, result);
    }

    @Test
    void getUserRole_ShouldReturnNull_WhenParticipantNotExists() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.empty());

        // When
        ParticipantRole result = tripPermissionService.getUserRole(tripId, userId);

        // Then
        assertNull(result);
    }

    @Test
    void hasRoleOrHigher_ShouldReturnTrue_WhenOrganizerHasOrganizerRole() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.hasRoleOrHigher(tripId, userId, ParticipantRole.ORGANIZER);

        // Then
        assertTrue(result);
    }

    @Test
    void hasRoleOrHigher_ShouldReturnTrue_WhenOrganizerHasMemberRole() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.hasRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);

        // Then
        assertTrue(result);
    }

    @Test
    void hasRoleOrHigher_ShouldReturnFalse_WhenMemberHasOrganizerRole() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.hasRoleOrHigher(tripId, userId, ParticipantRole.ORGANIZER);

        // Then
        assertFalse(result);
    }

    @Test
    void hasRoleOrHigher_ShouldReturnFalse_WhenUserNotParticipant() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.empty());

        // When
        boolean result = tripPermissionService.hasRoleOrHigher(tripId, userId, ParticipantRole.GUEST);

        // Then
        assertFalse(result);
    }

    @Test
    void canEditTrip_ShouldReturnTrue_WhenUserIsOrganizer() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.canEditTrip(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canEditTrip_ShouldReturnFalse_WhenUserIsMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canEditTrip(tripId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void canManageBudget_ShouldReturnTrue_WhenUserIsOrganizer() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.canManageBudget(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canAddExpenses_ShouldReturnTrue_WhenUserIsOrganizer() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.canAddExpenses(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canAddExpenses_ShouldReturnTrue_WhenUserIsMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canAddExpenses(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canAddExpenses_ShouldReturnFalse_WhenUserIsGuest() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(guestParticipant));

        // When
        boolean result = tripPermissionService.canAddExpenses(tripId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void canInviteParticipants_ShouldReturnTrue_WhenUserIsMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canInviteParticipants(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canManageParticipant_ShouldReturnTrue_WhenUserIsOrganizer() {
        // Given
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(memberParticipant));
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.canManageParticipant(tripId, userId, participantId);

        // Then
        assertTrue(result);
    }

    @Test
    void canManageParticipant_ShouldReturnTrue_WhenUserManagesSelf() {
        // Given
        Participant selfParticipant = Participant.builder()
                .id(participantId)
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .build();

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(selfParticipant));
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canManageParticipant(tripId, userId, participantId);

        // Then
        assertTrue(result);
    }

    @Test
    void canManageParticipant_ShouldReturnFalse_WhenParticipantNotExists() {
        // Given
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // When
        boolean result = tripPermissionService.canManageParticipant(tripId, userId, participantId);

        // Then
        assertFalse(result);
    }

    @Test
    void canManagePoints_ShouldReturnTrue_WhenUserIsMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canManagePoints(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canManagePoints_ShouldReturnFalse_WhenUserIsGuest() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(guestParticipant));

        // When
        boolean result = tripPermissionService.canManagePoints(tripId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void canAssignRole_ShouldReturnTrue_WhenUserIsOrganizer() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.canAssignRole(tripId, userId, ParticipantRole.MEMBER);

        // Then
        assertTrue(result);
    }

    @Test
    void canAssignRole_ShouldReturnFalse_WhenUserIsMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canAssignRole(tripId, userId, ParticipantRole.GUEST);

        // Then
        assertFalse(result);
    }

    @Test
    void isAcceptedParticipant_ShouldReturnTrue_WhenParticipantAccepted() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.isAcceptedParticipant(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void isAcceptedParticipant_ShouldReturnFalse_WhenParticipantPending() {
        // Given
        Participant pendingParticipant = Participant.builder()
                .userId(userId)
                .status(InvitationStatus.PENDING)
                .build();
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(pendingParticipant));

        // When
        boolean result = tripPermissionService.isAcceptedParticipant(tripId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void getAcceptedUserRole_ShouldReturnRole_WhenParticipantAccepted() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        ParticipantRole result = tripPermissionService.getAcceptedUserRole(tripId, userId);

        // Then
        assertEquals(ParticipantRole.ORGANIZER, result);
    }

    @Test
    void getAcceptedUserRole_ShouldReturnNull_WhenParticipantPending() {
        // Given
        Participant pendingParticipant = Participant.builder()
                .userId(userId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .build();
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(pendingParticipant));

        // When
        ParticipantRole result = tripPermissionService.getAcceptedUserRole(tripId, userId);

        // Then
        assertNull(result);
    }

    @Test
    void hasAcceptedRoleOrHigher_ShouldReturnTrue_WhenAcceptedOrganizerHasMemberRole() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(organizerParticipant));

        // When
        boolean result = tripPermissionService.hasAcceptedRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);

        // Then
        assertTrue(result);
    }

    @Test
    void hasAcceptedRoleOrHigher_ShouldReturnFalse_WhenPendingParticipant() {
        // Given
        Participant pendingParticipant = Participant.builder()
                .userId(userId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.PENDING)
                .build();
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(pendingParticipant));

        // When
        boolean result = tripPermissionService.hasAcceptedRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);

        // Then
        assertFalse(result);
    }

    @Test
    void canAddExpensesAsAccepted_ShouldReturnTrue_WhenAcceptedMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canAddExpensesAsAccepted(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canAddExpensesAsAccepted_ShouldReturnFalse_WhenAcceptedGuest() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(guestParticipant));

        // When
        boolean result = tripPermissionService.canAddExpensesAsAccepted(tripId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void canManagePointsAsAccepted_ShouldReturnTrue_WhenAcceptedMember() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(memberParticipant));

        // When
        boolean result = tripPermissionService.canManagePointsAsAccepted(tripId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void canManagePointsAsAccepted_ShouldReturnFalse_WhenAcceptedGuest() {
        // Given
        when(participantRepository.findByTripIdAndUserId(tripId, userId))
                .thenReturn(Optional.of(guestParticipant));

        // When
        boolean result = tripPermissionService.canManagePointsAsAccepted(tripId, userId);

        // Then
        assertFalse(result);
    }
}