package pl.sumatywny.travelmate.points.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sumatywny.travelmate.points.model.Point;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Integer> {
    @Query("select p from Point p where p.tripId = :tripId")
    List<Point> readPointsByTripId(@Param("tripId") int tripId);

}
