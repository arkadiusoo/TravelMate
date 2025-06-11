package pl.sumatywny.travelmate.trip.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.security.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepo;
    private final ParticipantRepository participantRepo;
    private final UserService userService;
    private final ParticipantService participantService;  // ✅ Added for email resolution

    public TripService(TripRepository tripRepo, ParticipantRepository participantRepo,
                       UserService userService, ParticipantService participantService) {
        this.tripRepo = tripRepo;
        this.participantRepo = participantRepo;
        this.userService = userService;
        this.participantService = participantService;  // ✅ Added dependency
    }

    public List<Trip> findAll() {
        return tripRepo.findAll();
    }

    public Trip findById(UUID id) {
        return tripRepo.findById(id).orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    public Trip create(Trip trip, UUID creatorUserId) {
        // Verify the user exists before creating the trip
        userService.findById(creatorUserId);  // This will throw if user doesn't exist

        Trip savedTrip = tripRepo.save(trip);

        // ✅ FIXED: Use ParticipantService to properly set email
        ParticipantDTO creatorDTO = ParticipantDTO.builder()
                .tripId(savedTrip.getId())
                .userId(creatorUserId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

        // This will automatically resolve and set the email
        participantService.addParticipant(creatorDTO, creatorUserId);

        return savedTrip;
    }

    public Trip update(UUID id, Trip trip) {
        Trip existing = findById(id);
        existing.setName(trip.getName());
        existing.setStartDate(trip.getStartDate());
        existing.setEndDate(trip.getEndDate());
        return tripRepo.save(existing);
    }

    public void delete(UUID id) {
        if (!tripRepo.existsById(id)) {
            throw new RuntimeException("Trip not found");
        }
        tripRepo.deleteById(id);
    }

    /**
     * ✅ NEW: Check if a user can access a specific trip
     * Only participants (any status) can access trip data
     * @param tripId The trip to check access for
     * @param userId The user requesting access
     * @return true if user can access the trip, false otherwise
     */
    public boolean canUserAccessTrip(UUID tripId, UUID userId) {
        return participantRepo.existsByTripIdAndUserId(tripId, userId);
    }

    /**
     * ✅ NEW: Get trips where user is a participant
     * @param userId The user ID to search for
     * @return List of trips where the user is a participant
     */
    public List<Trip> findTripsByUserId(UUID userId) {
        return tripRepo.findTripsByParticipantUserId(userId);
    }
}