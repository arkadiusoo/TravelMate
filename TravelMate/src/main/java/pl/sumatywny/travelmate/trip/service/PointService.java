package pl.sumatywny.travelmate.trip.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sumatywny.travelmate.trip.repository.PointRepository;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.TripRepository;

import java.util.List;

@Service
@Transactional
public class PointService {

    private final PointRepository pointRepo;
    private final TripRepository tripRepo;

    public PointService(PointRepository pointRepo, TripRepository tripRepo) {
        this.pointRepo = pointRepo;
        this.tripRepo = tripRepo;
    }

    public List<Point> findByTripId(Long tripId) {
        verifyTripExists(tripId);
        return pointRepo.findByTripId(tripId);
    }

    public Point findById(Long tripId, Long pointId) {
        verifyTripExists(tripId);
        return pointRepo.findById(pointId)
                .filter(p -> p.getTrip().getId().equals(tripId))
                .orElseThrow(() -> new RuntimeException());
    }

    public Point create(Long tripId, Point point) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException());
        point.setTrip(trip);
        return pointRepo.save(point);
    }

    public Point update(Long tripId, Long pointId, Point point) {
        Point existing = findById(tripId, pointId);
        existing.setTitle(point.getTitle());
        existing.setDate(point.getDate());
        existing.setDescription(point.getDescription());
        existing.setLatitude(point.getLatitude());
        existing.setLongitude(point.getLongitude());
        return pointRepo.save(existing);
    }

    public void delete(Long tripId, Long pointId) {
        verifyTripExists(tripId);
        if (!pointRepo.existsById(pointId)) {
            throw new RuntimeException();
        }
        pointRepo.deleteById(pointId);
    }

    private void verifyTripExists(Long tripId) {
        if (!tripRepo.existsById(tripId)) {
            throw new RuntimeException();
        }
    }
}
