package pl.sumatywny.travelmate.participant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.config.NotFoundException;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.UserService;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;
    private final TripPermissionService permissionService;
    private final UserService userService;

    /**
     * Dodaje nowego uczestnika do wycieczki.
     * Uczestnik może być zaproszony przez ID użytkownika lub adres email.
     * Działa tylko z zarejestrowanymi użytkownikami.
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

            // 🔥 NEW: Check if this is the first participant (trip creator)
            List<Participant> existingParticipants = participantRepository.findAllByTripId(participantDTO.getTripId());
            boolean isFirstParticipant = existingParticipants.isEmpty();
            boolean isCreatorAsOrganizer = participantDTO.getRole() == ParticipantRole.ORGANIZER
                    && participantDTO.getUserId() != null
                    && participantDTO.getUserId().equals(currentUserId);

            // Skip permission check for trip creator
            if (!(isFirstParticipant && isCreatorAsOrganizer)) {
                // Sprawdzenie uprawnień
                if (!permissionService.canInviteParticipants(participantDTO.getTripId(), currentUserId)) {
                    System.out.println("Permission check failed");
                    throw new IllegalStateException("Nie masz uprawnień do zapraszania uczestników");
                }
            } else {
                System.out.println("Skipping permission check - trip creator");
            }
            System.out.println("Permission check passed");

            // Tylko ORGANIZER może dodawać innych organizatorów (skip for trip creator)
            if (participantDTO.getRole() == ParticipantRole.ORGANIZER &&
                    !permissionService.hasRoleOrHigher(participantDTO.getTripId(), currentUserId, ParticipantRole.ORGANIZER) &&
                    !(isFirstParticipant && isCreatorAsOrganizer)) {
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

            // Jeśli podano tylko email, znajdź odpowiadający userId
            if (participantDTO.getUserId() == null && participantDTO.getEmail() != null) {
                UUID resolvedUserId = userService.findUserIdByEmail(participantDTO.getEmail());
                if (resolvedUserId == null) {
                    System.out.println("Email not found in registered users: " + participantDTO.getEmail());
                    throw new IllegalArgumentException("Nie znaleziono zarejestrowanego użytkownika z emailem: " + participantDTO.getEmail());
                }
                participantDTO.setUserId(resolvedUserId);
                System.out.println("Resolved email to userId: " + resolvedUserId);
            }

            // Jeśli podano tylko userId, znajdź odpowiadający email
            if (participantDTO.getUserId() != null && participantDTO.getEmail() == null) {
                String userEmail = userService.findEmailByUserId(participantDTO.getUserId());
                if (userEmail == null) {
                    System.out.println("UserId not found in registered users: " + participantDTO.getUserId());
                    throw new IllegalArgumentException("Nie znaleziono użytkownika z ID: " + participantDTO.getUserId());
                }
                participantDTO.setEmail(userEmail);
                System.out.println("Resolved userId to email: " + userEmail);
            }

            // Walidacja spójności - sprawdź czy email i userId należą do tego samego użytkownika
            if (participantDTO.getUserId() != null && participantDTO.getEmail() != null) {
                UUID emailUserId = userService.findUserIdByEmail(participantDTO.getEmail());
                if (!participantDTO.getUserId().equals(emailUserId)) {
                    System.out.println("Email and userId mismatch");
                    throw new IllegalArgumentException("Podany email i userId nie należą do tego samego użytkownika");
                }
            }

            // ✅ UPDATED: Check existing participation more intelligently
            Optional<Participant> existingParticipant = participantRepository.findByTripIdAndUserId(
                    participantDTO.getTripId(), participantDTO.getUserId());

            if (existingParticipant.isPresent()) {
                Participant existing = existingParticipant.get();
                InvitationStatus currentStatus = existing.getStatus();

                if (currentStatus == InvitationStatus.PENDING) {
                    throw new IllegalArgumentException("Użytkownik ma już oczekujące zaproszenie do tej wycieczki");
                } else if (currentStatus == InvitationStatus.ACCEPTED) {
                    throw new IllegalArgumentException("Użytkownik jest już uczestnikiem tej wycieczki");
                } else if (currentStatus == InvitationStatus.DECLINED) {
                    // ✅ ALLOW RE-INVITING: Update existing declined invitation to pending
                    System.out.println("Re-inviting previously declined user - updating existing record");
                    existing.setRole(participantDTO.getRole()); // Update role in case it changed
                    existing.setStatus(InvitationStatus.PENDING); // Reset to pending
                    Participant updated = participantRepository.save(existing);
                    return participantMapper.toDTO(updated);
                }
            }
            System.out.println("User participation check passed");

            // Domyślny status dla nowych zaproszeń to PENDING
            if (participantDTO.getStatus() == null) {
                System.out.println("Setting default status to PENDING");
                participantDTO.setStatus(InvitationStatus.PENDING);
            }

            // Utworzenie i zapisanie uczestnika (only for completely new participants)
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
    }/**
     * Aktualizuje rolę uczestnika używając ID uczestnika.
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

        return updateParticipantInternal(existing, updates, currentUserId);
    }

    /**
     * Aktualizuje rolę uczestnika używając email.
     *
     * @param tripId ID wycieczki
     * @param email Email uczestnika
     * @param updates Aktualizacje do zastosowania
     * @param currentUserId ID użytkownika wykonującego operację
     * @return Zaktualizowany uczestnik jako DTO
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli użytkownik nie ma uprawnień
     * @throws IllegalArgumentException Jeśli email nie należy do zarejestrowanego użytkownika
     */
    public ParticipantDTO updateParticipantRoleByEmail(UUID tripId, String email, ParticipantDTO updates, UUID currentUserId) {
        // Walidacja że email należy do zarejestrowanego użytkownika
        if (!userService.isRegisteredUser(email)) {
            throw new IllegalArgumentException("Nie znaleziono zarejestrowanego użytkownika z tym emailem");
        }

        Participant existing = participantRepository.findByTripIdAndEmail(tripId, email)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika z tym emailem w tej wycieczce"));

        // Sprawdzenie uprawnień
        if (!permissionService.canManageParticipant(existing.getTripId(), currentUserId, existing.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do zarządzania tym uczestnikiem");
        }

        return updateParticipantInternal(existing, updates, currentUserId);
    }

    /**
     * Wewnętrzna metoda do aktualizacji uczestnika.
     */
    private ParticipantDTO updateParticipantInternal(Participant existing, ParticipantDTO updates, UUID currentUserId) {
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
     * Usuwa uczestnika z wycieczki używając ID uczestnika.
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
     * Usuwa uczestnika z wycieczki używając email.
     *
     * @param tripId ID wycieczki
     * @param email Email uczestnika
     * @param currentUserId ID użytkownika wykonującego operację
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli użytkownik nie ma uprawnień
     * @throws IllegalArgumentException Jeśli email nie należy do zarejestrowanego użytkownika
     */
    public void removeParticipantByEmail(UUID tripId, String email, UUID currentUserId) {
        // Walidacja że email należy do zarejestrowanego użytkownika
        if (!userService.isRegisteredUser(email)) {
            throw new IllegalArgumentException("Nie znaleziono zarejestrowanego użytkownika z tym emailem");
        }

        Participant participant = participantRepository.findByTripIdAndEmail(tripId, email)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika z tym emailem w tej wycieczce"));

        // Sprawdzenie uprawnień
        if (!permissionService.canManageParticipant(participant.getTripId(), currentUserId, participant.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do usunięcia tego uczestnika");
        }

        participantRepository.delete(participant);
    }

// Replace your existing getParticipantsByTrip method with this:
    /**
     * Zwraca listę wszystkich uczestników wycieczki z populated user details.
     *
     * @param tripId ID wycieczki
     * @return Lista wszystkich uczestników wycieczki jako DTO z pełnymi danymi użytkownika
     */
    public List<ParticipantDTO> getParticipantsByTrip(UUID tripId) {
        return participantRepository.findAllByTripId(tripId)
                .stream()
                .map(participant -> {
                    ParticipantDTO dto = participantMapper.toDTO(participant);

                    // Get full user details
                    User user = userService.findById(participant.getUserId());

                    // Populate all user information
                    dto.setEmail(user.getEmail());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Przetwarza odpowiedź na zaproszenie do wycieczki używając ID uczestnika.
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

        // Tylko użytkownik może odpowiadać na własne zaproszenia
        if (!participant.getUserId().equals(currentUserId)) {
            throw new IllegalStateException("Możesz odpowiadać tylko na własne zaproszenia");
        }

        // Aktualizacja statusu
        participant.setStatus(status);

        // NEW: Set joinedAt when accepting invitation
        if (status == InvitationStatus.ACCEPTED) {
            participant.setJoinedAt(LocalDateTime.now());
        }

        Participant updated = participantRepository.save(participant);

        // Zwrócenie zaktualizowanego DTO
        return participantMapper.toDTO(updated);
    }
    /**
     * Przetwarza odpowiedź na zaproszenie do wycieczki używając email.
     *
     * @param tripId ID wycieczki
     * @param email Email uczestnika
     * @param status Nowy status (ACCEPTED lub DECLINED)
     * @param currentUserId ID użytkownika wykonującego operację
     * @return Zaktualizowany ParticipantDTO
     * @throws IllegalArgumentException Jeśli status jest nieprawidłowy lub email nie należy do zarejestrowanego użytkownika
     * @throws NotFoundException Jeśli uczestnik nie istnieje
     * @throws IllegalStateException Jeśli zaproszenie nie jest oczekujące lub użytkownik nie ma uprawnień
     */
    public ParticipantDTO respondToInvitationByEmail(UUID tripId, String email, InvitationStatus status, UUID currentUserId) {
        // Tylko ACCEPTED lub DECLINED są poprawnymi odpowiedziami
        if (status == InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Status musi być ACCEPTED lub DECLINED");
        }

        // Walidacja że email należy do zarejestrowanego użytkownika
        if (!userService.isRegisteredUser(email)) {
            throw new IllegalArgumentException("Nie znaleziono zarejestrowanego użytkownika z tym emailem");
        }

        // Znalezienie rekordu uczestnika
        Participant participant = participantRepository.findByTripIdAndEmail(tripId, email)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono uczestnika z tym emailem w tej wycieczce"));

        // Tylko oczekujące zaproszenia mogą być aktualizowane
        if (participant.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Tylko oczekujące zaproszenia mogą być aktualizowane");
        }

        // Tylko użytkownik może odpowiadać na własne zaproszenia
        if (!participant.getUserId().equals(currentUserId)) {
            throw new IllegalStateException("Możesz odpowiadać tylko na własne zaproszenia");
        }

        // Aktualizacja statusu
        participant.setStatus(status);
        if (status == InvitationStatus.ACCEPTED) {
            participant.setJoinedAt(LocalDateTime.now());
        }
        Participant updated = participantRepository.save(participant);

        // Zwrócenie zaktualizowanego DTO
        return participantMapper.toDTO(updated);
    }


}