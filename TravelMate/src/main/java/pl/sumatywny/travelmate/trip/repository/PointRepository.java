package pl.sumatywny.travelmate.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.trip.model.Point;

import java.util.List;
import java.util.UUID;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByTripId(UUID tripId);  // Changed Long to UUID

    Point getPointById(UUID poointId);
}