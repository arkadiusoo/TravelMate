package pl.sumatywny.travelmate.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.trip.model.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {}
