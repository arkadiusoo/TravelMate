package pl.sumatywny.travelmate.participant_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
public class TripPermissionServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private TripPermissionService permissionService;

    // Common test data
    private final UUID tripId = UUID.fromString("b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3");
    private final UUID organizerId = UUID.fromString("e8c40d9a-11e2-47cb-90fc-1c6d5bd6b0ae");
    private final UUID memberId = UUID.fromString("f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f");
    private final UUID guestId = UUID.fromString("313e92d3-36a7-4108-9d98-0b5554a4eb1d");
    private final UUID nonParticipantId = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private Participant organizer;
    private Participant member;
    private Participant guest;
    private UUID organizerParticipantId;
    private UUID memberParticipantId;
    private UUID guestParticipantId;

    @BeforeEach
    void setUp() {
        // Set up participant test data
        organizerParticipantId = UUID.randomUUID();
        memberParticipantId = UUID.randomUUID();
        guestParticipantId = UUID.randomUUID();

        organizer = Participant.builder()
                .id(organizerParticipantId)
                .tripId(tripId)
                .userId(organizerId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        member = Participant.builder()
                .id(memberParticipantId)
                .tripId(tripId)
                .userId(memberId)
                .role(ParticipantRole.MEMBER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        guest = Participant.builder()
                .id(guestParticipantId)
                .tripId(tripId)
                .userId(guestId)
                .role(ParticipantRole.GUEST)
                .status(InvitationStatus.ACCEPTED)
                .build();

        // Set up mock repository responses
        when(participantRepository.findByTripIdAndUserId(tripId, organizerId))
                .thenReturn(Optional.of(organizer));
        when(participantRepository.findByTripIdAndUserId(tripId, memberId))
                .thenReturn(Optional.of(member));
        when(participantRepository.findByTripIdAndUserId(tripId, guestId))
                .thenReturn(Optional.of(guest));
        when(participantRepository.findByTripIdAndUserId(tripId, nonParticipantId))
                .thenReturn(Optional.empty());

        // Set up mock responses for findById used in canManageParticipant
        when(participantRepository.findById(organizerParticipantId))
                .thenReturn(Optional.of(organizer));
        when(participantRepository.findById(memberParticipantId))
                .thenReturn(Optional.of(member));
        when(participantRepository.findById(guestParticipantId))
                .thenReturn(Optional.of(guest));
    }

    @Nested
    @DisplayName("Get User Role Tests")
    class GetUserRoleTests {

        @Test
        @DisplayName("Should return ORGANIZER role for organizer user")
        void getUserRole_returnsOrganizerRoleForOrganizerUser() {
            ParticipantRole role = permissionService.getUserRole(tripId, organizerId);
            assertEquals(ParticipantRole.ORGANIZER, role);
        }

        @Test
        @DisplayName("Should return MEMBER role for member user")
        void getUserRole_returnsMemberRoleForMemberUser() {
            ParticipantRole role = permissionService.getUserRole(tripId, memberId);
            assertEquals(ParticipantRole.MEMBER, role);
        }

        @Test
        @DisplayName("Should return GUEST role for guest user")
        void getUserRole_returnsGuestRoleForGuestUser() {
            ParticipantRole role = permissionService.getUserRole(tripId, guestId);
            assertEquals(ParticipantRole.GUEST, role);
        }

        @Test
        @DisplayName("Should return null for non-participant user")
        void getUserRole_returnsNullForNonParticipantUser() {
            ParticipantRole role = permissionService.getUserRole(tripId, nonParticipantId);
            assertNull(role);
        }
    }

    @Nested
    @DisplayName("Has Role Or Higher Tests")
    class HasRoleOrHigherTests {

        @Test
        @DisplayName("ORGANIZER has ORGANIZER role or higher")
        void hasRoleOrHigher_organizerHasOrganizerRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.ORGANIZER);
            assertTrue(result);
        }

        @Test
        @DisplayName("ORGANIZER has MEMBER role or higher")
        void hasRoleOrHigher_organizerHasMemberRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.MEMBER);
            assertTrue(result);
        }

        @Test
        @DisplayName("ORGANIZER has GUEST role or higher")
        void hasRoleOrHigher_organizerHasGuestRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, organizerId, ParticipantRole.GUEST);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER does not have ORGANIZER role or higher")
        void hasRoleOrHigher_memberDoesNotHaveOrganizerRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.ORGANIZER);
            assertFalse(result);
        }

        @Test
        @DisplayName("MEMBER has MEMBER role or higher")
        void hasRoleOrHigher_memberHasMemberRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.MEMBER);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER has GUEST role or higher")
        void hasRoleOrHigher_memberHasGuestRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, memberId, ParticipantRole.GUEST);
            assertTrue(result);
        }

        @Test
        @DisplayName("GUEST does not have ORGANIZER role or higher")
        void hasRoleOrHigher_guestDoesNotHaveOrganizerRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, guestId, ParticipantRole.ORGANIZER);
            assertFalse(result);
        }

        @Test
        @DisplayName("GUEST does not have MEMBER role or higher")
        void hasRoleOrHigher_guestDoesNotHaveMemberRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, guestId, ParticipantRole.MEMBER);
            assertFalse(result);
        }

        @Test
        @DisplayName("GUEST has GUEST role or higher")
        void hasRoleOrHigher_guestHasGuestRoleOrHigher() {
            boolean result = permissionService.hasRoleOrHigher(tripId, guestId, ParticipantRole.GUEST);
            assertTrue(result);
        }

        @Test
        @DisplayName("Non-participant does not have any role")
        void hasRoleOrHigher_nonParticipantDoesNotHaveAnyRole() {
            boolean result = permissionService.hasRoleOrHigher(tripId, nonParticipantId, ParticipantRole.GUEST);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Can Edit Trip Tests")
    class CanEditTripTests {

        @Test
        @DisplayName("ORGANIZER can edit trip")
        void canEditTrip_organizerCanEditTrip() {
            boolean result = permissionService.canEditTrip(tripId, organizerId);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER cannot edit trip")
        void canEditTrip_memberCannotEditTrip() {
            boolean result = permissionService.canEditTrip(tripId, memberId);
            assertFalse(result);
        }

        @Test
        @DisplayName("GUEST cannot edit trip")
        void canEditTrip_guestCannotEditTrip() {
            boolean result = permissionService.canEditTrip(tripId, guestId);
            assertFalse(result);
        }

        @Test
        @DisplayName("Non-participant cannot edit trip")
        void canEditTrip_nonParticipantCannotEditTrip() {
            boolean result = permissionService.canEditTrip(tripId, nonParticipantId);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Can Manage Budget Tests")
    class CanManageBudgetTests {

        @Test
        @DisplayName("ORGANIZER can manage budget")
        void canManageBudget_organizerCanManageBudget() {
            boolean result = permissionService.canManageBudget(tripId, organizerId);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER cannot manage budget")
        void canManageBudget_memberCannotManageBudget() {
            boolean result = permissionService.canManageBudget(tripId, memberId);
            assertFalse(result);
        }

        @Test
        @DisplayName("GUEST cannot manage budget")
        void canManageBudget_guestCannotManageBudget() {
            boolean result = permissionService.canManageBudget(tripId, guestId);
            assertFalse(result);
        }

        @Test
        @DisplayName("Non-participant cannot manage budget")
        void canManageBudget_nonParticipantCannotManageBudget() {
            boolean result = permissionService.canManageBudget(tripId, nonParticipantId);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Can Add Expenses Tests")
    class CanAddExpensesTests {

        @Test
        @DisplayName("ORGANIZER can add expenses")
        void canAddExpenses_organizerCanAddExpenses() {
            boolean result = permissionService.canAddExpenses(tripId, organizerId);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER can add expenses")
        void canAddExpenses_memberCanAddExpenses() {
            boolean result = permissionService.canAddExpenses(tripId, memberId);
            assertTrue(result);
        }

        @Test
        @DisplayName("GUEST cannot add expenses")
        void canAddExpenses_guestCannotAddExpenses() {
            boolean result = permissionService.canAddExpenses(tripId, guestId);
            assertFalse(result);
        }

        @Test
        @DisplayName("Non-participant cannot add expenses")
        void canAddExpenses_nonParticipantCannotAddExpenses() {
            boolean result = permissionService.canAddExpenses(tripId, nonParticipantId);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Can Invite Participants Tests")
    class CanInviteParticipantsTests {

        @Test
        @DisplayName("ORGANIZER can invite participants")
        void canInviteParticipants_organizerCanInviteParticipants() {
            boolean result = permissionService.canInviteParticipants(tripId, organizerId);
            assertTrue(result);
        }

        @Test
        @DisplayName("MEMBER cannot invite participants")
        void canInviteParticipants_memberCannotInviteParticipants() {
            boolean result = permissionService.canInviteParticipants(tripId, memberId);
            assertFalse(result);
        }

        @Test
        @DisplayName("GUEST cannot invite participants")
        void canInviteParticipants_guestCannotInviteParticipants() {
            boolean result = permissionService.canInviteParticipants(tripId, guestId);
            assertFalse(result);
        }

        @Test
        @DisplayName("Non-participant cannot invite participants")
        void canInviteParticipants_nonParticipantCannotInviteParticipants() {
            boolean result = permissionService.canInviteParticipants(tripId, nonParticipantId);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Can Manage Participant Tests")
    class CanManageParticipantTests {

        @Test
        @DisplayName("ORGANIZER can manage any participant")
        void canManageParticipant_organizerCanManageAnyParticipant() {
            // Organizer can manage member
            boolean canManageMember = permissionService.canManageParticipant(tripId, organizerId, memberParticipantId);
            assertTrue(canManageMember);

            // Organizer can manage guest
            boolean canManageGuest = permissionService.canManageParticipant(tripId, organizerId, guestParticipantId);
            assertTrue(canManageGuest);

            // Organizer can manage self
            boolean canManageSelf = permissionService.canManageParticipant(tripId, organizerId, organizerParticipantId);
            assertTrue(canManageSelf);
        }

        @Test
        @DisplayName("MEMBER can only manage self")
        void canManageParticipant_memberCanOnlyManageSelf() {
            // Member can manage self
            boolean canManageSelf = permissionService.canManageParticipant(tripId, memberId, memberParticipantId);
            assertTrue(canManageSelf);

            // Member cannot manage organizer
            boolean canManageOrganizer = permissionService.canManageParticipant(tripId, memberId, organizerParticipantId);
            assertFalse(canManageOrganizer);

            // Member cannot manage guest
            boolean canManageGuest = permissionService.canManageParticipant(tripId, memberId, guestParticipantId);
            assertFalse(canManageGuest);
        }

        @Test
        @DisplayName("GUEST can only manage self")
        void canManageParticipant_guestCanOnlyManageSelf() {
            // Guest can manage self
            boolean canManageSelf = permissionService.canManageParticipant(tripId, guestId, guestParticipantId);
            assertTrue(canManageSelf);

            // Guest cannot manage organizer
            boolean canManageOrganizer = permissionService.canManageParticipant(tripId, guestId, organizerParticipantId);
            assertFalse(canManageOrganizer);

            // Guest cannot manage member
            boolean canManageMember = permissionService.canManageParticipant(tripId, guestId, memberParticipantId);
            assertFalse(canManageMember);
        }

        @Test
        @DisplayName("Non-participant cannot manage any participant")
        void canManageParticipant_nonParticipantCannotManageAnyParticipant() {
            boolean canManageOrganizer = permissionService.canManageParticipant(tripId, nonParticipantId, organizerParticipantId);
            assertFalse(canManageOrganizer);

            boolean canManageMember = permissionService.canManageParticipant(tripId, nonParticipantId, memberParticipantId);
            assertFalse(canManageMember);

            boolean canManageGuest = permissionService.canManageParticipant(tripId, nonParticipantId, guestParticipantId);
            assertFalse(canManageGuest);
        }

        @Test
        @DisplayName("Nobody can manage non-existent participant")
        void canManageParticipant_nobodyCanManageNonExistentParticipant() {
            UUID nonExistentParticipantId = UUID.randomUUID();
            when(participantRepository.findById(nonExistentParticipantId)).thenReturn(Optional.empty());

            boolean organizerResult = permissionService.canManageParticipant(tripId, organizerId, nonExistentParticipantId);
            assertFalse(organizerResult);

            boolean memberResult = permissionService.canManageParticipant(tripId, memberId, nonExistentParticipantId);
            assertFalse(memberResult);

            boolean guestResult = permissionService.canManageParticipant(tripId, guestId, nonExistentParticipantId);
            assertFalse(guestResult);
        }
    }

    @Nested
    @DisplayName("Can Assign Role Tests")
    class CanAssignRoleTests {

        @Test
        @DisplayName("ORGANIZER can assign any role")
        void canAssignRole_organizerCanAssignAnyRole() {
            // Organizer can assign ORGANIZER role
            boolean canAssignOrganizer = permissionService.canAssignRole(tripId, organizerId, ParticipantRole.ORGANIZER);
            assertTrue(canAssignOrganizer);

            // Organizer can assign MEMBER role
            boolean canAssignMember = permissionService.canAssignRole(tripId, organizerId, ParticipantRole.MEMBER);
            assertTrue(canAssignMember);

            // Organizer can assign GUEST role
            boolean canAssignGuest = permissionService.canAssignRole(tripId, organizerId, ParticipantRole.GUEST);
            assertTrue(canAssignGuest);
        }

        @Test
        @DisplayName("MEMBER cannot assign any role")
        void canAssignRole_memberCannotAssignAnyRole() {
            // Member cannot assign ORGANIZER role
            boolean canAssignOrganizer = permissionService.canAssignRole(tripId, memberId, ParticipantRole.ORGANIZER);
            assertFalse(canAssignOrganizer);

            // Member cannot assign MEMBER role
            boolean canAssignMember = permissionService.canAssignRole(tripId, memberId, ParticipantRole.MEMBER);
            assertFalse(canAssignMember);

            // Member cannot assign GUEST role
            boolean canAssignGuest = permissionService.canAssignRole(tripId, memberId, ParticipantRole.GUEST);
            assertFalse(canAssignGuest);
        }

        @Test
        @DisplayName("GUEST cannot assign any role")
        void canAssignRole_guestCannotAssignAnyRole() {
            // Guest cannot assign ORGANIZER role
            boolean canAssignOrganizer = permissionService.canAssignRole(tripId, guestId, ParticipantRole.ORGANIZER);
            assertFalse(canAssignOrganizer);

            // Guest cannot assign MEMBER role
            boolean canAssignMember = permissionService.canAssignRole(tripId, guestId, ParticipantRole.MEMBER);
            assertFalse(canAssignMember);

            // Guest cannot assign GUEST role
            boolean canAssignGuest = permissionService.canAssignRole(tripId, guestId, ParticipantRole.GUEST);
            assertFalse(canAssignGuest);
        }

        @Test
        @DisplayName("Non-participant cannot assign any role")
        void canAssignRole_nonParticipantCannotAssignAnyRole() {
            // Non-participant cannot assign ORGANIZER role
            boolean canAssignOrganizer = permissionService.canAssignRole(tripId, nonParticipantId, ParticipantRole.ORGANIZER);
            assertFalse(canAssignOrganizer);

            // Non-participant cannot assign MEMBER role
            boolean canAssignMember = permissionService.canAssignRole(tripId, nonParticipantId, ParticipantRole.MEMBER);
            assertFalse(canAssignMember);

            // Non-participant cannot assign GUEST role
            boolean canAssignGuest = permissionService.canAssignRole(tripId, nonParticipantId, ParticipantRole.GUEST);
            assertFalse(canAssignGuest);
        }
    }
}