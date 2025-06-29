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
    private final ParticipantService participantService;

    public TripService(TripRepository tripRepo, ParticipantRepository participantRepo,
                       UserService userService, ParticipantService participantService) {
        this.tripRepo = tripRepo;
        this.participantRepo = participantRepo;
        this.userService = userService;
        this.participantService = participantService;
    }

    public List<Trip> findAll() {
        return tripRepo.findAll();
    }

    public Trip findById(UUID id) {
        return tripRepo.findById(id).orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    public Trip create(Trip trip, UUID creatorUserId) {
        userService.findById(creatorUserId);

        Trip savedTrip = tripRepo.save(trip);

        ParticipantDTO creatorDTO = ParticipantDTO.builder()
                .tripId(savedTrip.getId())
                .userId(creatorUserId)
                .role(ParticipantRole.ORGANIZER)
                .status(InvitationStatus.ACCEPTED)
                .build();

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

    public boolean canUserAccessTrip(UUID tripId, UUID userId) {
        return participantRepo.existsByTripIdAndUserId(tripId, userId);
    }

    public List<Trip> findTripsByUserId(UUID userId) {
        return tripRepo.findTripsByParticipantUserId(userId);
    }
}