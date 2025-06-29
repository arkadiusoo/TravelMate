package pl.sumatywny.travelmate.participant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripPermissionService {

    private final ParticipantRepository participantRepository;

    /**
     * Pobiera rolę użytkownika w wycieczce
     *
     * @param tripId ID wycieczki
     * @param userId ID użytkownika
     * @return Rola użytkownika lub null jeśli nie jest uczestnikiem
     */
    public ParticipantRole getUserRole(UUID tripId, UUID userId) {

        return participantRepository.findByTripIdAndUserId(tripId, userId)
                .map(participant -> participant.getRole())
                .orElse(null);
    }


    /**
     * Sprawdza czy użytkownik ma określoną rolę lub wyższą
     *
     * @param tripId ID wycieczki
     * @param userId ID użytkownika
     * @param minimumRole Minimalna wymagana rola
     * @return true jeśli użytkownik ma wymaganą rolę lub wyższą
     */
    public boolean hasRoleOrHigher(UUID tripId, UUID userId, ParticipantRole minimumRole) {
        ParticipantRole userRole = getUserRole(tripId, userId);

        // Jeśli użytkownik nie jest uczestnikiem wycieczki
        if (userRole == null) {
            return false;
        }

        // Sprawdzenie hierarchii ról (ORGANIZER > MEMBER > GUEST)
        // Im niższy indeks w enum, tym wyższa rola
        return userRole.ordinal() <= minimumRole.ordinal();
    }

    /**
     * Sprawdza czy użytkownik może edytować informacje o wycieczce
     */
    public boolean canEditTrip(UUID tripId, UUID userId) {
        // Tylko ORGANIZER może edytować informacje o wycieczce
        return hasRoleOrHigher(tripId, userId, ParticipantRole.ORGANIZER);
    }

    /**
     * Sprawdza czy użytkownik może zarządzać budżetem
     */
    public boolean canManageBudget(UUID tripId, UUID userId) {
        // ORGANIZER może zarządzać budżetem
        return hasRoleOrHigher(tripId, userId, ParticipantRole.ORGANIZER);
    }

    /**
     * Sprawdza czy użytkownik może dodawać wydatki
     */
    public boolean canAddExpenses(UUID tripId, UUID userId) {
        // ORGANIZER i MEMBER mogą dodawać wydatki
        return hasRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);
    }
    /**
     * Sprawdza czy użytkownik może zapraszać uczestników
     */
    public boolean canInviteParticipants(UUID tripId, UUID userId) {
        // ORGANIZER może zapraszać uczestników
        return hasRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);
        //return true;
    }

    /**
     * Sprawdza czy użytkownik może zarządzać konkretnym uczestnikiem
     *
     * @param tripId ID wycieczki
     * @param userId ID sprawdzanego użytkownika
     * @param participantId ID uczestnika, którego chcemy zarządzać
     * @return true jeśli użytkownik ma uprawnienia
     */
    public boolean canManageParticipant(UUID tripId, UUID userId, UUID participantId) {

        if (participantRepository.findById(participantId).isEmpty()) {
            return false; // Can't manage a non-existent participant
        }

        ParticipantRole userRole = getUserRole(tripId, userId);

        // Jeśli użytkownik nie jest uczestnikiem
        if (userRole == null) {
            return false;
        }

        // ORGANIZER może zarządzać wszystkimi uczestnikami
        if (userRole == ParticipantRole.ORGANIZER) {
            return true;
        }

        // Pozostali użytkownicy mogą zarządzać tylko sobą
        // Sprawdzamy, czy participantId odpowiada rekordowi z userId użytkownika
        return participantRepository.findById(participantId)
                .map(participant -> participant.getUserId().equals(userId))
                .orElse(false);
    }


    /**
     * Sprawdza czy użytkownik może dodawać/edytować punkty
     */
    public boolean canManagePoints(UUID tripId, UUID userId) {
        // ORGANIZER i MEMBER mogą zarządzać punktami, GUEST nie
        return hasRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);
    }


    /**
     * Sprawdza czy użytkownik może przydzielać określoną rolę
     */
    public boolean canAssignRole(UUID tripId, UUID userId, ParticipantRole roleToAssign) {
        ParticipantRole userRole = getUserRole(tripId, userId);

        // Jeśli użytkownik nie jest uczestnikiem
        if (userRole == null) {
            return false;
        }

        // Tylko ORGANIZER może przydzielać role
        if (userRole != ParticipantRole.ORGANIZER) {
            return false;
        }

        // ORGANIZER może przydzielać wszystkie role
        return true;

    }

    // Add these new methods to TripPermissionService.java

    /**
     * Sprawdza czy użytkownik jest zaakceptowanym uczestnikiem wycieczki
     * @param tripId ID wycieczki
     * @param userId ID użytkownika
     * @return true jeśli użytkownik ma status ACCEPTED
     */
    public boolean isAcceptedParticipant(UUID tripId, UUID userId) {
        return participantRepository.findByTripIdAndUserId(tripId, userId)
                .map(participant -> participant.getStatus() == InvitationStatus.ACCEPTED)
                .orElse(false);
    }

    /**
     * Pobiera rolę użytkownika TYLKO jeśli ma status ACCEPTED
     * @param tripId ID wycieczki
     * @param userId ID użytkownika
     * @return Rola użytkownika lub null jeśli nie jest zaakceptowanym uczestnikiem
     */
    public ParticipantRole getAcceptedUserRole(UUID tripId, UUID userId) {
        return participantRepository.findByTripIdAndUserId(tripId, userId)
                .filter(participant -> participant.getStatus() == InvitationStatus.ACCEPTED)
                .map(participant -> participant.getRole())
                .orElse(null);
    }

    /**
     * Sprawdza czy zaakceptowany użytkownik ma określoną rolę lub wyższą
     * @param tripId ID wycieczki
     * @param userId ID użytkownika
     * @param minimumRole Minimalna wymagana rola
     * @return true jeśli użytkownik jest zaakceptowany I ma wymaganą rolę lub wyższą
     */
    public boolean hasAcceptedRoleOrHigher(UUID tripId, UUID userId, ParticipantRole minimumRole) {
        ParticipantRole userRole = getAcceptedUserRole(tripId, userId);

        // Jeśli użytkownik nie jest zaakceptowanym uczestnikiem
        if (userRole == null) {
            return false;
        }

        // Sprawdzenie hierarchii ról (ORGANIZER > MEMBER > GUEST)
        return userRole.ordinal() <= minimumRole.ordinal();
    }

    /**
     * Sprawdza czy zaakceptowany użytkownik może dodawać wydatki
     */
    public boolean canAddExpensesAsAccepted(UUID tripId, UUID userId) {
        // Tylko zaakceptowani ORGANIZER i MEMBER mogą dodawać wydatki
        return hasAcceptedRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);
    }

    /**
     * Sprawdza czy zaakceptowany użytkownik może zarządzać punktami
     */
    public boolean canManagePointsAsAccepted(UUID tripId, UUID userId) {
        // Tylko zaakceptowani ORGANIZER i MEMBER mogą zarządzać punktami
        return hasAcceptedRoleOrHigher(tripId, userId, ParticipantRole.MEMBER);
    }



}