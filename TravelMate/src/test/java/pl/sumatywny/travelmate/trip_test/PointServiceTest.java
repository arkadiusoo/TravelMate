package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.PointRepository;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;
    private PointRepository pointRepo;
    private TripRepository tripRepo;

    private final UUID tripId = UUID.randomUUID();
    private final Long pointId = 1L;

    private Trip trip;
    private Point point;

    @BeforeEach
    void setUp() {
        pointRepo = mock(PointRepository.class);
        tripRepo = mock(TripRepository.class);
        pointService = new PointService(pointRepo, tripRepo);

        trip = new Trip();
        trip.setId(tripId);

        point = new Point();
        point.setId(pointId);
        point.setTrip(trip);
        point.setTitle("Test Point");
        point.setDate(LocalDate.now());
    }

    @Test
    void shouldFindPointsByTripId() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findByTripId(tripId)).thenReturn(List.of(point));

        List<Point> points = pointService.findByTripId(tripId);

        assertThat(points).hasSize(1).contains(point);
    }

    @Test
    void shouldThrowIfTripNotExistOnFindByTripId() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        assertThatThrownBy(() -> pointService.findByTripId(tripId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFindPointById() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(pointId)).thenReturn(Optional.of(point));

        Point result = pointService.findById(tripId, pointId);

        assertThat(result).isEqualTo(point);
    }

    @Test
    void shouldThrowIfPointNotBelongToTrip() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        Trip otherTrip = new Trip();
        otherTrip.setId(UUID.randomUUID());
        point.setTrip(otherTrip);
        when(pointRepo.findById(pointId)).thenReturn(Optional.of(point));

        assertThatThrownBy(() -> pointService.findById(tripId, pointId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreatePoint() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));
        when(pointRepo.save(any())).thenReturn(point);

        Point result = pointService.create(tripId, point);

        assertThat(result).isEqualTo(point);
        assertThat(result.getTrip().getId()).isEqualTo(tripId);
    }

    @Test
    void shouldUpdatePoint() {
        Point updated = new Point();
        updated.setTitle("Updated");
        updated.setLatitude(1.0);
        updated.setLongitude(2.0);
        updated.setDate(LocalDate.of(2025, 6, 1));
        updated.setDescription("Updated desc");

        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(pointId)).thenReturn(Optional.of(point));
        when(pointRepo.save(any())).thenReturn(point);

        Point result = pointService.update(tripId, pointId, updated);

        assertThat(result.getTitle()).isEqualTo("Updated");
    }

    @Test
    void shouldDeletePoint() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.existsById(pointId)).thenReturn(true);

        pointService.delete(tripId, pointId);

        verify(pointRepo).deleteById(pointId);
    }

    @Test
    void shouldMarkPointAsVisited() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(pointId)).thenReturn(Optional.of(point));
        when(pointRepo.save(any())).thenReturn(point);

        Point result = pointService.markVisited(tripId, pointId);

        assertThat(result.isVisited()).isTrue();
    }
}
