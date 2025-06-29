package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.participant.dto.ParticipantDTO;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.participant.service.ParticipantService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.TripRepository;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TripServiceTest {

    private TripRepository tripRepo;
    private ParticipantRepository participantRepo;
    private UserService userService;
    private ParticipantService participantService;
    private TripService tripService;

    private UUID tripId;
    private UUID userId;
    private Trip sampleTrip;

    @BeforeEach
    void setUp() {
        tripRepo = mock(TripRepository.class);
        participantRepo = mock(ParticipantRepository.class);
        userService = mock(UserService.class);
        participantService = mock(ParticipantService.class);
        tripService = new TripService(tripRepo, participantRepo, userService, participantService);

        tripId = UUID.randomUUID();
        userId = UUID.randomUUID();

        sampleTrip = new Trip();
        sampleTrip.setId(tripId);
        sampleTrip.setName("Sample Trip");
        sampleTrip.setStartDate(LocalDate.of(2025, 7, 1));
        sampleTrip.setEndDate(LocalDate.of(2025, 7, 10));
    }

    @Test
    void shouldFindAllTrips() {
        when(tripRepo.findAll()).thenReturn(List.of(sampleTrip));

        List<Trip> trips = tripService.findAll();

        assertThat(trips).hasSize(1).contains(sampleTrip);
    }

    @Test
    void shouldFindTripById() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.of(sampleTrip));

        Trip result = tripService.findById(tripId);

        assertThat(result).isEqualTo(sampleTrip);
    }

    @Test
    void shouldThrowWhenTripNotFound() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.findById(tripId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip not found");
    }

    @Test
    void shouldCreateTripAndAddOrganizerParticipant() {
        when(userService.findById(userId)).thenReturn(null); // doesn't matter, only checks if call doesn't throw
        when(tripRepo.save(any())).thenReturn(sampleTrip);

        Trip created = tripService.create(sampleTrip, userId);

        assertThat(created).isEqualTo(sampleTrip);

        verify(participantService).addParticipant(argThat(dto ->
                dto.getTripId().equals(tripId) &&
                        dto.getUserId().equals(userId) &&
                        dto.getRole() == ParticipantRole.ORGANIZER &&
                        dto.getStatus() == InvitationStatus.ACCEPTED
        ), eq(userId));
    }

    @Test
    void shouldUpdateTripFields() {
        Trip updated = new Trip();
        updated.setName("Updated Trip");
        updated.setStartDate(LocalDate.of(2025, 7, 5));
        updated.setEndDate(LocalDate.of(2025, 7, 15));

        when(tripRepo.findById(tripId)).thenReturn(Optional.of(sampleTrip));
        when(tripRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.update(tripId, updated);

        assertThat(result.getName()).isEqualTo("Updated Trip");
        assertThat(result.getStartDate()).isEqualTo(updated.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(updated.getEndDate());
    }

    @Test
    void shouldDeleteTrip() {
        when(tripRepo.existsById(tripId)).thenReturn(true);

        tripService.delete(tripId);

        verify(tripRepo).deleteById(tripId);
    }

    @Test
    void shouldThrowOnDeleteIfTripNotExist() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        assertThatThrownBy(() -> tripService.delete(tripId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip not found");
    }

    @Test
    void shouldReturnTrueIfUserCanAccessTrip() {
        when(participantRepo.existsByTripIdAndUserId(tripId, userId)).thenReturn(true);

        assertThat(tripService.canUserAccessTrip(tripId, userId)).isTrue();
    }

    @Test
    void shouldReturnFalseIfUserCannotAccessTrip() {
        when(participantRepo.existsByTripIdAndUserId(tripId, userId)).thenReturn(false);

        assertThat(tripService.canUserAccessTrip(tripId, userId)).isFalse();
    }

    @Test
    void shouldFindTripsByUserId() {
        when(tripRepo.findTripsByParticipantUserId(userId)).thenReturn(List.of(sampleTrip));

        List<Trip> result = tripService.findTripsByUserId(userId);

        assertThat(result).containsExactly(sampleTrip);
    }
}
