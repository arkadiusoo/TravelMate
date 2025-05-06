package pl.sumatywny.travelmate.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.trip.model.Point;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByTripId(Long tripId);
}
