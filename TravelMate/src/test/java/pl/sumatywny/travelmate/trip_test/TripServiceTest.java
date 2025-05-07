package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripService tripService;

    private Trip trip;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        trip = new Trip();
        trip.setId(1L);
        trip.setName("Summer Vacation");
        trip.setStartDate(LocalDate.of(2025, 7, 1));
        trip.setEndDate(LocalDate.of(2025, 7, 14));
    }

    @Test
    void shouldFindAllTrips() {
        when(tripRepository.findAll()).thenReturn(List.of(trip));

        List<Trip> result = tripService.findAll();

        assertThat(result).containsExactly(trip);
    }

    @Test
    void shouldFindTripById() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        Trip result = tripService.findById(1L);

        assertThat(result).isEqualTo(trip);
    }

    @Test
    void shouldThrowWhenTripNotFoundById() {
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.findById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldCreateTrip() {
        when(tripRepository.save(trip)).thenReturn(trip);

        Trip result = tripService.create(trip);

        assertThat(result).isEqualTo(trip);
    }

    @Test
    void shouldUpdateTrip() {
        Trip updated = new Trip();
        updated.setName("Winter Break");
        updated.setStartDate(LocalDate.of(2025, 12, 20));
        updated.setEndDate(LocalDate.of(2025, 12, 31));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Winter Break");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 12, 20));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void shouldThrowWhenDeletingNonExistingTrip() {
        when(tripRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> tripService.delete(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldDeleteTrip() {
        when(tripRepository.existsById(1L)).thenReturn(true);

        tripService.delete(1L);

        verify(tripRepository).deleteById(1L);
    }
}
