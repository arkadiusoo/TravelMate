package pl.sumatywny.travelmate.participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.Participant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for accessing and manipulating Participant entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    
    /**
     * Finds all participants for a specific trip.
     *
     * @param tripId The ID of the trip
     * @return List of all participants in the trip
     */
    List<Participant> findAllByTripId(UUID tripId);
    
    /**
     * Finds a specific participant by both trip ID and user ID.
     *
     * @param tripId The ID of the trip
     * @param userId The ID of the user
     * @return Optional containing the participant if found
     */
    Optional<Participant> findByTripIdAndUserId(UUID tripId, UUID userId);
    
    /**
     * Checks if a user is already a participant in a specific trip.
     *
     * @param tripId The ID of the trip
     * @param userId The ID of the user
     * @return True if the user is already a participant in the trip
     */
    boolean existsByTripIdAndUserId(UUID tripId, UUID userId);
    
    /**
     * Finds all invitations for a user with a specific status.
     * Useful for finding pending invitations for a user.
     *
     * @param userId The ID of the user
     * @param status The invitation status to filter by
     * @return List of matching participant records
     */
    List<Participant> findByUserIdAndStatus(UUID userId, InvitationStatus status);
    
    /**
     * Finds a specific participant by both trip ID and email.
     *
     * @param tripId The ID of the trip
     * @param email The email of the participant
     * @return Optional containing the participant if found
     */
    Optional<Participant> findByTripIdAndEmail(UUID tripId, String email);
    
    /**
     * Checks if an email is already a participant in a specific trip.
     *
     * @param tripId The ID of the trip
     * @param email The email to check
     * @return True if the email is already a participant in the trip
     */
    boolean existsByTripIdAndEmail(UUID tripId, String email);
    Participant getParticipantByEmail(String email);
}