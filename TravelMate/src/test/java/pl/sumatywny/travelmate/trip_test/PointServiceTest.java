package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.PointRepository;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private PointService pointService;

    private Trip trip;
    private Point point;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        trip = new Trip();
        trip.setId(1L);
        trip.setName("Trip to Spain");

        point = new Point();
        point.setId(10L);
        point.setTitle("Sagrada Familia");
        point.setDescription("Visit cathedral");
        point.setLatitude(41.4036);
        point.setLongitude(2.1744);
        point.setDate(LocalDate.of(2025, 6, 20));
        point.setTrip(trip);
    }

    @Test
    void shouldFindPointsByTripId() {
        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.findByTripId(1L)).thenReturn(List.of(point));

        List<Point> result = pointService.findByTripId(1L);

        assertThat(result).containsExactly(point);
    }

    @Test
    void shouldThrowWhenTripNotFoundInFindByTripId() {
        when(tripRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> pointService.findByTripId(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFindPointById() {
        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.findById(10L)).thenReturn(Optional.of(point));

        Point result = pointService.findById(1L, 10L);

        assertThat(result).isEqualTo(point);
    }

    @Test
    void shouldThrowWhenPointNotBelongToTrip() {
        Trip anotherTrip = new Trip();
        anotherTrip.setId(2L);
        point.setTrip(anotherTrip);

        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.findById(10L)).thenReturn(Optional.of(point));

        assertThatThrownBy(() -> pointService.findById(1L, 10L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreatePoint() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pointRepository.save(point)).thenReturn(point);

        Point result = pointService.create(1L, point);

        assertThat(result).isEqualTo(point);
        assertThat(result.getTrip()).isEqualTo(trip);
    }

    @Test
    void shouldThrowWhenCreatingPointForMissingTrip() {
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.create(1L, point))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldUpdatePoint() {
        Point updated = new Point();
        updated.setTitle("New title");
        updated.setDescription("New desc");
        updated.setLatitude(50.0);
        updated.setLongitude(19.0);
        updated.setDate(LocalDate.of(2025, 7, 1));

        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.findById(10L)).thenReturn(Optional.of(point));
        when(pointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Point result = pointService.update(1L, 10L, updated);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getLatitude()).isEqualTo(50.0);
        assertThat(result.getLongitude()).isEqualTo(19.0);
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2025, 7, 1));
    }

    @Test
    void shouldDeletePoint() {
        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.existsById(10L)).thenReturn(true);

        pointService.delete(1L, 10L);

        verify(pointRepository).deleteById(10L);
    }

    @Test
    void shouldThrowWhenDeletingPointOfNonexistentTrip() {
        when(tripRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> pointService.delete(1L, 10L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowWhenDeletingNonexistentPoint() {
        when(tripRepository.existsById(1L)).thenReturn(true);
        when(pointRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> pointService.delete(1L, 10L))
                .isInstanceOf(RuntimeException.class);
    }
}
