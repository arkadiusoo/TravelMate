package pl.sumatywny.travelmate.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {}  // Changed Long to UUID