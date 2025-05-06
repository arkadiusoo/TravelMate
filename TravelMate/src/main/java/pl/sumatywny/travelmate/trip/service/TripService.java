package pl.sumatywny.travelmate.trip.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.util.List;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepo;

    public TripService(TripRepository tripRepo) {
        this.tripRepo = tripRepo;
    }

    public List<Trip> findAll() {
        return tripRepo.findAll();
    }

    public Trip findById(Long id) {
        return tripRepo.findById(id).orElseThrow(() -> new RuntimeException());
    }

    public Trip create(Trip trip) {
        return tripRepo.save(trip);
    }

    public Trip update(Long id, Trip trip) {
        Trip existing = findById(id);
        existing.setName(trip.getName());
        existing.setStartDate(trip.getStartDate());
        existing.setEndDate(trip.getEndDate());
        return tripRepo.save(existing);
    }

    public void delete(Long id) {
        if (!tripRepo.existsById(id)) {
            throw new RuntimeException();
        }
        tripRepo.deleteById(id);
    }
}
