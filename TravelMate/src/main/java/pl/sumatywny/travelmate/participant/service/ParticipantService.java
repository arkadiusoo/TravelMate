package pl.sumatywny.travelmate.participant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import pl.sumatywny.travelmate.config.NotFoundException;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;
    private final TripPermissionService permissionService;

    /**
     * Dodaje nowego uczestnika do wycieczki.
     * Uczestnik może być zaproszony przez ID użytkownika lub adres email.
     *
     * @param participantDTO Dane nowego uczestnika
     * @param currentUserId ID użytkownika wykonującego operację
     * @return Utworzony uczestnik jako DTO
     * @throws IllegalArgumentException Jeśli uczestnik już istnieje w wycieczce lub dane są nieprawidłowe
     * @throws IllegalStateException Jeśli użytkownik nie ma uprawnień
     */
    public ParticipantDTO addParticipant(ParticipantDTO participantDTO, UUID currentUserId) {
        try {
            System.out.println("Starting addParticipant with DTO: " + participantDTO);
            System.out.println("Current user ID: " + currentUserId);

            // Sprawdzenie uprawnień
            if (!permissionService.canInviteParticipants(participantDTO.getTripId(), currentUserId)) {
                System.out.println("Permission check failed");
                throw new IllegalStateException("Nie masz uprawnień do zapraszania uczestników");
            }
            System.out.println("Permission check passed");

            // Tylko ORGANIZER może dodawać innych organizatorów
            if (participantDTO.getRole() == ParticipantRole.ORGANIZER &&
                    !permissionService.hasRoleOrHigher(participantDTO.getTripId(), currentUserId, ParticipantRole.ORGANIZER)) {
                System.out.println("Organizer permission check failed");
                throw new IllegalStateException("Tylko organizatorzy mogą dodawać innych organizatorów");
            }
            System.out.println("Role check passed");

            // Podstawowa walidacja - musi być podane userId lub email
            if (participantDTO.getUserId() == null &&
                    (participantDTO.getEmail() == null || participantDTO.getEmail().isEmpty())) {
                System.out.println("Validation failed - no userId or email");
                throw new IllegalArgumentException("Należy podać userId lub email");
            }
            System.out.println("Basic validation passed");

            // Sprawdzenie czy użytkownik jest już uczestnikiem tej wycieczki (jeśli podano userId)
            if (participantDTO.getUserId() != null &&
                    participantRepository.existsByTripIdAndUserId(
                            participantDTO.getTripId(), participantDTO.getUserId())) {
                System.out.println("User already exists in trip");
                throw new IllegalArgumentException("Użytkownik jest już uczestnikiem tej wycieczki");
            }
            System.out.println("User existence check passed");

            // Domyślny status dla nowych zaproszeń to PENDING
            if (participantDTO.getStatus() == null) {
                System.out.println("Setting default status to PENDING");
                participantDTO.setStatus(InvitationStatus.PENDING);
            }

            // Utworzenie i zapisanie uczestnika
            System.out.println("Converting DTO to entity");
            Participant participant = participantMapper.toEntity(participantDTO);
            System.out.println("Created entity: " + participant);
            System.out.println("Saving to repository");
            Participant saved = participantRepository.save(participant);
            System.out.println("Saved entity with ID: " + saved.getId());
            return participantMapper.toDTO(saved);
        } catch (Exception e) {
            System.err.println("Error in addParticipant: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * Aktualizuje rolę uczestnika.
     *
     * @param id ID uczestnika do zaktualizowania
     * @param updates Aktualizacje do zastosowania
     * @param currentUserId ID użytkownika wykonującego operację
     * @return Zaktualizowany uczestnik jako DTO
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli użytkownik nie ma uprawnień
     */
    public ParticipantDTO updateParticipantRole(UUID id, ParticipantDTO updates, UUID currentUserId) {
        Participant existing = participantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika"));

        // Sprawdzenie uprawnień
        if (!permissionService.canManageParticipant(existing.getTripId(), currentUserId, id)) {
            throw new IllegalStateException("Nie masz uprawnień do zarządzania tym uczestnikiem");
        }

        // Sprawdzenie czy może przydzielić rolę (jeśli podano rolę)
        if (updates.getRole() != null) {
            if (!permissionService.canAssignRole(existing.getTripId(), currentUserId, updates.getRole())) {
                throw new IllegalStateException("Nie masz uprawnień do przydzielania tej roli");
            }
            existing.setRole(updates.getRole());
        }

        // Zapisanie i zwrócenie
        Participant updated = participantRepository.save(existing);
        return participantMapper.toDTO(updated);
    }

    /**
     * Usuwa uczestnika z wycieczki.
     *
     * @param id ID uczestnika do usunięcia
     * @param currentUserId ID użytkownika wykonującego operację
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli użytkownik nie ma uprawnień
     */
    public void removeParticipant(UUID id, UUID currentUserId) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika"));

        // Sprawdzenie uprawnień
        if (!permissionService.canManageParticipant(participant.getTripId(), currentUserId, id)) {
            throw new IllegalStateException("Nie masz uprawnień do usunięcia tego uczestnika");
        }

        participantRepository.deleteById(id);
    }

    /**
     * Zwraca listę wszystkich uczestników wycieczki.
     *
     * @param tripId ID wycieczki
     * @return Lista wszystkich uczestników wycieczki jako DTO
     */
    public List<ParticipantDTO> getParticipantsByTrip(UUID tripId) {
        return participantRepository.findAllByTripId(tripId)
                .stream()
                .map(participantMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Przetwarza odpowiedź na zaproszenie do wycieczki.
     *
     * @param participantId ID rekordu uczestnika
     * @param status Nowy status (ACCEPTED lub DECLINED)
     * @param currentUserId ID użytkownika wykonującego operację
     * @return Zaktualizowany ParticipantDTO
     * @throws IllegalArgumentException Jeśli status jest nieprawidłowy
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli zaproszenie nie jest oczekujące lub użytkownik nie ma uprawnień
     */
    public ParticipantDTO respondToInvitation(UUID participantId, InvitationStatus status, UUID currentUserId) {
        // Tylko ACCEPTED lub DECLINED są poprawnymi odpowiedziami
        if (status == InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Status musi być ACCEPTED lub DECLINED");
        }

        // Znalezienie rekordu uczestnika
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika"));

        // Tylko oczekujące zaproszenia mogą być aktualizowane
        if (participant.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Tylko oczekujące zaproszenia mogą być aktualizowane");
        }

        // Użytkownik może odpowiadać tylko na własne zaproszenia
        if (!participant.getUserId().equals(currentUserId)) {
            throw new IllegalStateException("Możesz odpowiadać tylko na własne zaproszenia");
        }

        // Aktualizacja statusu
        participant.setStatus(status);
        Participant updated = participantRepository.save(participant);

        // Zwrócenie zaktualizowanego DTO
        return participantMapper.toDTO(updated);
    }
}