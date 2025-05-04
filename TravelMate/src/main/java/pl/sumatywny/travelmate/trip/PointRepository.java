package pl.sumatywny.travelmate.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByTripId(Long tripId);
}
