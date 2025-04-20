package pl.sumatywny.travelmate.participant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.config.NotFoundException;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;

    /**
     * Adds a new participant to a trip.
     * The participant can be invited either by user ID or email.
     *
     * @param participantDTO Data for the new participant
     * @return The created participant as DTO
     * @throws IllegalArgumentException If the participant already exists in the trip
     */
    public ParticipantDTO addParticipant(ParticipantDTO participantDTO) {
        if (participantDTO.getUserId() == null &&
                (participantDTO.getEmail() == null || participantDTO.getEmail().isEmpty())) {
            throw new IllegalArgumentException("Either userId or email must be provided");
        }

        // Check if this user is already a participant in this trip (if userId is provided)
        if (participantDTO.getUserId() != null &&
                participantRepository.existsByTripIdAndUserId(
                        participantDTO.getTripId(), participantDTO.getUserId())) {
            throw new IllegalArgumentException("User is already a participant in this trip");
        }

        // For new invites, the status should be PENDING by default
        if (participantDTO.getStatus() == null) {
            participantDTO.setStatus(InvitationStatus.PENDING);
        }

        // Create and save the participant
        Participant participant = participantMapper.toEntity(participantDTO);
        Participant saved = participantRepository.save(participant);
        return participantMapper.toDTO(saved);
    }

    public ParticipantDTO updateParticipantRole(UUID id, ParticipantDTO updates) {
        Participant existing = participantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        // Update the role if provided
        if (updates.getRole() != null) {
            existing.setRole(updates.getRole());
        }

        // Save and return
        Participant updated = participantRepository.save(existing);
        return participantMapper.toDTO(updated);
    }

    /**
     * Removes a participant from a trip.
     *
     * @param id ID of the participant to remove
     * @throws NotFoundException If the participant doesn't exist
     */
    public void removeParticipant(UUID id) {
        if (!participantRepository.existsById(id)) {
            throw new NotFoundException("Participant not found");
        }
        participantRepository.deleteById(id);
    }

    /**
     * Lists all participants for a trip.
     *
     * @param tripId The ID of the trip
     * @return List of all participants in the trip
     */
    public List<ParticipantDTO> getParticipantsByTrip(UUID tripId) {
        return participantRepository.findAllByTripId(tripId)
                .stream()
                .map(participantMapper::toDTO)
                .collect(Collectors.toList());
    }
}