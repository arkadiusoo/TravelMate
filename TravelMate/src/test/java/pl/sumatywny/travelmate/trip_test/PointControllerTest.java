package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.security.config.JwtAuthenticationFilter;
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.trip.controller.PointController;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.PointService;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private TripService tripService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID tripId;
    private Point samplePoint;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);

        samplePoint = new Point();
        samplePoint.setId(10L);
        samplePoint.setTitle("Test Point");
        samplePoint.setDescription("Some description");
        samplePoint.setLatitude(50.0);
        samplePoint.setLongitude(19.0);
        samplePoint.setDate(LocalDate.of(2025, 5, 7));
        samplePoint.setTrip(trip);
    }

    @Test
    void shouldReturnAllPoints() throws Exception {
        Mockito.when(pointService.findByTripId(tripId))
                .thenReturn(List.of(samplePoint));

        mockMvc.perform(get("/api/trips/{tripId}/points", tripId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Point"));

        Mockito.verify(pointService).findByTripId(tripId);
    }

    @Test
    void shouldReturnOnePoint() throws Exception {
        Mockito.when(pointService.findById(tripId, 10L))
                .thenReturn(samplePoint);

        mockMvc.perform(get("/api/trips/{tripId}/points/{pointId}", tripId, 10L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Point"));

        Mockito.verify(pointService).findById(tripId, 10L);
    }

    @Test
    void shouldCreatePoint() throws Exception {
        Mockito.when(pointService.create(eq(tripId), any(Point.class)))
                .thenReturn(samplePoint);

        mockMvc.perform(post("/api/trips/{tripId}/points", tripId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Test Point"));

        Mockito.verify(pointService).create(eq(tripId), any(Point.class));
    }

    @Test
    void shouldUpdatePoint() throws Exception {
        Point updated = new Point();
        updated.setId(10L);
        updated.setTrip(samplePoint.getTrip());
        updated.setTitle("Updated Title");
        updated.setDescription(samplePoint.getDescription());
        updated.setLatitude(samplePoint.getLatitude());
        updated.setLongitude(samplePoint.getLongitude());
        updated.setDate(samplePoint.getDate());

        Mockito.when(pointService.update(eq(tripId), eq(10L), any(Point.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/trips/{tripId}/points/{pointId}", tripId, 10L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        Mockito.verify(pointService).update(eq(tripId), eq(10L), any(Point.class));
    }

    @Test
    void shouldDeletePoint() throws Exception {
        mockMvc.perform(delete("/api/trips/{tripId}/points/{pointId}", tripId, 10L).with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(pointService).delete(tripId, 10L);
    }
}
