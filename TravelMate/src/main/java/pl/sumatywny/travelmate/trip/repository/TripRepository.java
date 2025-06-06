package pl.sumatywny.travelmate.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    @Query("SELECT DISTINCT t FROM Trip t JOIN Participant p ON t.id = p.tripId WHERE p.userId = :userId")
    List<Trip> findTripsByParticipantUserId(@Param("userId") UUID userId);
}

