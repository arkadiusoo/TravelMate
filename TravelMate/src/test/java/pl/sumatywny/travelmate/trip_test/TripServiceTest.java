package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepo;

    @Mock
    private ParticipantRepository participantRepo;

    @Mock
    private UserService userService;

    @Mock
    private ParticipantService participantService;

    @InjectMocks
    private TripService tripService;

    private UUID tripId;
    private Trip trip;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        trip = new Trip();
        trip.setId(tripId);
        trip.setName("Test Trip");
        trip.setStartDate(LocalDate.of(2025, 7, 1));
        trip.setEndDate(LocalDate.of(2025, 7, 10));
    }

    @Test
    void findAll_returnsListOfTrips() {
        List<Trip> expected = Collections.singletonList(trip);
        when(tripRepo.findAll()).thenReturn(expected);

        List<Trip> actual = tripService.findAll();
        assertEquals(expected, actual);
        verify(tripRepo, times(1)).findAll();
    }

    @Test
    void findById_existingId_returnsTrip() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));

        Trip found = tripService.findById(tripId);
        assertEquals(trip, found);
        verify(tripRepo).findById(tripId);
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(tripRepo.findById(tripId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tripService.findById(tripId));
        assertEquals("Trip not found", ex.getMessage());
        verify(tripRepo).findById(tripId);
    }

    @Test
    void create_savesTripAndAddsCreatorAsParticipant() {
        UUID creatorId = UUID.randomUUID();
        Trip unsaved = new Trip();
        unsaved.setName("New Trip");
        unsaved.setStartDate(LocalDate.of(2025, 8, 1));
        unsaved.setEndDate(LocalDate.of(2025, 8, 5));
        Trip saved = new Trip();
        saved.setId(tripId);
        saved.setName(unsaved.getName());
        saved.setStartDate(unsaved.getStartDate());
        saved.setEndDate(unsaved.getEndDate());

        when(userService.findById(creatorId)).thenReturn(null);
        when(tripRepo.save(unsaved)).thenReturn(saved);

        Trip result = tripService.create(unsaved, creatorId);
        assertEquals(saved, result);

        verify(userService).findById(creatorId);
        verify(tripRepo).save(unsaved);
        verify(participantService).addParticipant(
                argThat(dto -> dto.getTripId().equals(tripId)
                        && dto.getUserId().equals(creatorId)
                        && dto.getRole() == ParticipantRole.ORGANIZER
                        && dto.getStatus() == InvitationStatus.ACCEPTED),
                eq(creatorId)
        );
    }

    @Test
    void update_existingTrip_updatesFields() {
        Trip updatedInfo = new Trip();
        updatedInfo.setName("Updated Trip");
        updatedInfo.setStartDate(LocalDate.of(2025, 9, 1));
        updatedInfo.setEndDate(LocalDate.of(2025, 9, 10));

        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRepo.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.update(tripId, updatedInfo);
        assertEquals("Updated Trip", result.getName());
        assertEquals(LocalDate.of(2025, 9, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 9, 10), result.getEndDate());

        verify(tripRepo).findById(tripId);
        verify(tripRepo).save(trip);
    }

    @Test
    void delete_existingId_deletesTrip() {
        when(tripRepo.existsById(tripId)).thenReturn(true);

        tripService.delete(tripId);

        verify(tripRepo).existsById(tripId);
        verify(tripRepo).deleteById(tripId);
    }

    @Test
    void delete_nonExistingId_throwsException() {
        when(tripRepo.existsById(tripId)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tripService.delete(tripId));
        assertEquals("Trip not found", ex.getMessage());

        verify(tripRepo).existsById(tripId);
        verify(tripRepo, never()).deleteById(any());
    }

    @Test
    void canUserAccessTrip_returnsTrueIfExists() {
        UUID userId = UUID.randomUUID();
        when(participantRepo.existsByTripIdAndUserId(tripId, userId)).thenReturn(true);

        assertTrue(tripService.canUserAccessTrip(tripId, userId));
        verify(participantRepo).existsByTripIdAndUserId(tripId, userId);
    }

    @Test
    void canUserAccessTrip_returnsFalseIfNotExists() {
        UUID userId = UUID.randomUUID();
        when(participantRepo.existsByTripIdAndUserId(tripId, userId)).thenReturn(false);

        assertFalse(tripService.canUserAccessTrip(tripId, userId));
        verify(participantRepo).existsByTripIdAndUserId(tripId, userId);
    }

    @Test
    void findTripsByUserId_returnsTripsList() {
        UUID userId = UUID.randomUUID();
        List<Trip> expected = Collections.singletonList(trip);
        when(tripRepo.findTripsByParticipantUserId(userId)).thenReturn(expected);

        List<Trip> actual = tripService.findTripsByUserId(userId);
        assertEquals(expected, actual);
        verify(tripRepo).findTripsByParticipantUserId(userId);
    }
}
