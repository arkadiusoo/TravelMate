package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.PointRepository;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepo;

    @Mock
    private TripRepository tripRepo;

    @InjectMocks
    private PointService service;

    private UUID tripId;
    private Trip trip;
    private Point point;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        trip = new Trip();
        trip.setId(tripId);

        point = new Point();
        point.setId(1L);
        point.setTrip(trip);
        point.setTitle("Old Title");
        point.setDate(LocalDate.of(2025, 7, 1));
        point.setDescription("Desc");
        point.setLatitude(10.0);
        point.setLongitude(20.0);
        point.setVisited(false);
    }

    @Test
    void findByTripId_whenTripExists_returnsPoints() {
        List<Point> points = List.of(point);
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findByTripId(tripId)).thenReturn(points);

        List<Point> result = service.findByTripId(tripId);

        assertThat(result).isSameAs(points);
        verify(tripRepo).existsById(tripId);
        verify(pointRepo).findByTripId(tripId);
    }

    @Test
    void findByTripId_whenTripNotExists_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.findByTripId(tripId));
        verify(tripRepo).existsById(tripId);
        verifyNoMoreInteractions(pointRepo);
    }

    @Test
    void findById_whenTripExistsAndPointMatches_returnsPoint() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.of(point));

        Point found = service.findById(tripId, 1L);

        assertThat(found).isSameAs(point);
        verify(tripRepo).existsById(tripId);
        verify(pointRepo).findById(1L);
    }

    @Test
    void findById_whenTripNotExists_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.findById(tripId, 1L));
        verify(tripRepo).existsById(tripId);
        verifyNoMoreInteractions(pointRepo);
    }

    @Test
    void findById_whenPointExistsButTripMismatch_throws() {
        UUID otherTripId = UUID.randomUUID();
        Trip otherTrip = new Trip();
        otherTrip.setId(otherTripId);
        point.setTrip(otherTrip);

        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.of(point));

        assertThrows(RuntimeException.class, () -> service.findById(tripId, 1L));
        verify(pointRepo).findById(1L);
    }

    @Test
    void create_whenTripFound_savesAndReturnsPoint() {
        Point toCreate = new Point();
        toCreate.setTitle("New");
        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));
        when(pointRepo.save(any(Point.class))).thenAnswer(inv -> inv.getArgument(0));

        Point created = service.create(tripId, toCreate);

        assertThat(created.getTrip()).isSameAs(trip);
        assertThat(created.getTitle()).isEqualTo("New");
        verify(tripRepo).findById(tripId);
        verify(pointRepo).save(toCreate);
    }

    @Test
    void create_whenTripNotFound_throws() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(tripId, new Point()));
        verify(tripRepo).findById(tripId);
        verifyNoMoreInteractions(pointRepo);
    }

    @Test
    void update_whenExists_updatesFieldsAndSaves() {
        Point updatedInfo = new Point();
        updatedInfo.setTitle("New Title");
        updatedInfo.setDate(LocalDate.of(2025, 7, 2));
        updatedInfo.setDescription("New Desc");
        updatedInfo.setLatitude(30.0);
        updatedInfo.setLongitude(40.0);

        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.of(point));
        when(pointRepo.save(any(Point.class))).thenAnswer(inv -> inv.getArgument(0));

        Point updated = service.update(tripId, 1L, updatedInfo);

        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2025, 7, 2));
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getLatitude()).isEqualTo(30.0);
        assertThat(updated.getLongitude()).isEqualTo(40.0);
        verify(pointRepo).save(point);
    }

    @Test
    void update_whenPointNotFound_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update(tripId, 1L, new Point()));
    }

    @Test
    void delete_whenTripNotExists_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.delete(tripId, 1L));
        verify(tripRepo).existsById(tripId);
    }

    @Test
    void delete_whenPointNotExists_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.delete(tripId, 1L));
        verify(pointRepo).existsById(1L);
    }

    @Test
    void delete_whenExists_deletesPoint() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.existsById(1L)).thenReturn(true);

        service.delete(tripId, 1L);

        verify(pointRepo).deleteById(1L);
    }

    @Test
    void markVisited_whenExists_setsVisitedAndSaves() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.of(point));
        when(pointRepo.save(any(Point.class))).thenAnswer(inv -> inv.getArgument(0));

        Point visited = service.markVisited(tripId, 1L);

        assertThat(visited.isVisited()).isTrue();
        verify(pointRepo).save(point);
    }

    @Test
    void markVisited_whenNotFound_throws() {
        when(tripRepo.existsById(tripId)).thenReturn(true);
        when(pointRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.markVisited(tripId, 1L));
    }
}
