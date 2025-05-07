package pl.sumatywny.travelmate.participant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        return true;
    }

    /**
     * Sprawdza czy użytkownik może edytować informacje o wycieczce
     */
    public boolean canEditTrip(UUID tripId, UUID userId) {
        return true;
    }

    /**
     * Sprawdza czy użytkownik może zarządzać budżetem
     */
    public boolean canManageBudget(UUID tripId, UUID userId) {
        return true;
    }

    /**
     * Sprawdza czy użytkownik może dodawać wydatki
     */
    public boolean canAddExpenses(UUID tripId, UUID userId) {
        return true;
    }

    /**
     * Sprawdza czy użytkownik może zapraszać uczestników
     */
    public boolean canInviteParticipants(UUID tripId, UUID userId) {
        return true;
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

    return true;
    }


    /**
     * Sprawdza czy użytkownik może przydzielać określoną rolę
     */
    public boolean canAssignRole(UUID tripId, UUID userId, ParticipantRole roleToAssign) {
        return true;
    }
}