package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.security.config.JwtAuthenticationFilter;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.trip.controller.PointController;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.PointService;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;
    @MockBean
    private PointService pointService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(invocation -> {
            var req   = invocation.getArgument(0, ServletRequest.class);
            var resp  = invocation.getArgument(1, ServletResponse.class);
            var chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(req, resp);
            return null;
        }).when(jwtAuthenticationFilter)
                .doFilter(any(ServletRequest.class),
                        any(ServletResponse.class),
                        any(FilterChain.class));
    }

    private static final String USER_EMAIL = "user@example.com";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final long POINT_ID = 1L;

    private User mockUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        return user;
    }

    private Trip sampleTrip() {
        return Trip.builder()
                .id(UUID.randomUUID())
                .name("Test Trip")
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .tripBudget(1000.0)
                .build();
    }

    private Point samplePoint(Trip trip) {
        return Point.builder()
                .id(POINT_ID)
                .title("Test Point")
                .description("Some description")
                .latitude(50.0)
                .longitude(19.0)
                .date(LocalDate.of(2025, 5, 7))
                .trip(trip)
                .build();
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnAllPoints() throws Exception {
        Trip trip = sampleTrip();
        Point point = samplePoint(trip);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.findTripsByUserId(USER_ID)).thenReturn(List.of(trip));
        when(pointService.findByTripId(TRIP_ID)).thenReturn(List.of(point));
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/trips/{TRIP_ID}/points", TRIP_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Point"));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnOnePoint() throws Exception {
        Trip trip = sampleTrip();
        Point point = samplePoint(trip);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(pointService.findById(TRIP_ID, POINT_ID)).thenReturn(point);
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/trips/{TRIP_ID}/points/{pointId}", TRIP_ID, POINT_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Point"));

        verify(pointService).findById(TRIP_ID, POINT_ID);
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreatePoint() throws Exception {
        Trip trip = sampleTrip();
        Point point = samplePoint(trip);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(pointService.findById(TRIP_ID, POINT_ID)).thenReturn(point);
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);
        when(pointService.create(eq(TRIP_ID), any(Point.class))).thenReturn(point);

        mockMvc.perform(post("/api/trips/{TRIP_ID}/points", TRIP_ID).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(point)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(POINT_ID))
                .andExpect(jsonPath("$.title").value("Test Point"));

        verify(pointService).create(eq(TRIP_ID), any(Point.class));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdatePoint() throws Exception {
        Trip trip = sampleTrip();
        Point point = samplePoint(trip);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(pointService.findById(TRIP_ID, POINT_ID)).thenReturn(point);
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);

        Point updated = new Point();
        updated.setId(POINT_ID);
        updated.setTrip(point.getTrip());
        updated.setTitle("Updated Title");
        updated.setDescription(point.getDescription());
        updated.setLatitude(point.getLatitude());
        updated.setLongitude(point.getLongitude());
        updated.setDate(point.getDate());

        when(pointService.update(eq(TRIP_ID), eq(POINT_ID), any(Point.class))).thenReturn(updated);

        mockMvc.perform(put("/api/trips/{TRIP_ID}/points/{pointId}", TRIP_ID, POINT_ID).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(pointService).update(eq(TRIP_ID), eq(POINT_ID), any(Point.class));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldDeletePoint() throws Exception {
        Trip trip = sampleTrip();
        Point point = samplePoint(trip);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(pointService.findById(TRIP_ID, POINT_ID)).thenReturn(point);
        when(tripService.canUserAccessTrip(TRIP_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/trips/{TRIP_ID}/points/{pointId}", TRIP_ID, POINT_ID).with(csrf()))
                .andExpect(status().isOk());

        verify(pointService).delete(TRIP_ID, POINT_ID);
    }
}
