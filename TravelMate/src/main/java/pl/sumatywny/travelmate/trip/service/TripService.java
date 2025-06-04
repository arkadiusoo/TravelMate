package pl.sumatywny.travelmate.trip.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import java.util.UUID;
import pl.sumatywny.travelmate.security.service.UserService;

import java.util.List;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepo;
    private final ParticipantRepository participantRepo;
    private final UserService userService;  // Add this field


    public TripService(TripRepository tripRepo, ParticipantRepository participantRepo, UserService userService) {  // Add UserService
        this.tripRepo = tripRepo;
        this.participantRepo = participantRepo;
        this.userService = userService;
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

        Participant creator = new Participant();
        creator.setTripId(savedTrip.getId());
        creator.setUserId(creatorUserId);
        creator.setRole(ParticipantRole.ORGANIZER);
        creator.setStatus(InvitationStatus.ACCEPTED);

        participantRepo.save(creator);

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
}
