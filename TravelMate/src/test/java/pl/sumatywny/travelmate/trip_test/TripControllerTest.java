package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import pl.sumatywny.travelmate.trip.controller.TripController;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @DisplayName("GET /api/trips - Success")
    @WithMockUser(username = USER_EMAIL)
    void testGetAllTripsSuccess() throws Exception {
        Trip trip = sampleTrip();
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.findTripsByUserId(USER_ID)).thenReturn(List.of(trip));

        mockMvc.perform(get("/api/trips").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(trip.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Test Trip"));
    }

    @Test
    @DisplayName("POST /api/trips - Success")
    @WithMockUser(username = USER_EMAIL)
    void testCreateTripSuccess() throws Exception {
        Trip trip = sampleTrip();
        Trip requestTrip = Trip.builder()
                .name(trip.getName())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripBudget(trip.getTripBudget())
                .build();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.create(any(Trip.class), eq(USER_ID))).thenReturn(trip);

        mockMvc.perform(post("/api/trips").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTrip)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trip.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Trip"));
    }

    @Test
    @DisplayName("GET /api/trips/{id} - Success")
    @WithMockUser(username = USER_EMAIL)
    void testGetOneTripSuccess() throws Exception {
        Trip trip = sampleTrip();
        UUID tripId = trip.getId();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(true);
        when(tripService.findById(tripId)).thenReturn(trip);

        mockMvc.perform(get("/api/trips/{id}", tripId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId.toString()));
    }

    @Test
    @DisplayName("GET /api/trips/{id} - Access Denied")
    @WithMockUser(username = USER_EMAIL)
    void testGetOneTripAccessDenied() throws Exception {
        UUID tripId = UUID.randomUUID();
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(false);

        mockMvc.perform(get("/api/trips/{id}", tripId).with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /api/trips/{id} - Success")
    @WithMockUser(username = USER_EMAIL)
    void testUpdateTripSuccess() throws Exception {
        Trip trip = sampleTrip();
        UUID tripId = trip.getId();

        Trip updateTrip = Trip.builder()
                .name("Updated Trip")
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripBudget(trip.getTripBudget())
                .build();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(true);
        Trip updatedTrip = Trip.builder()
                .id(trip.getId())
                .name("Updated Trip")
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .tripBudget(trip.getTripBudget())
                .build();
        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(true);
        when(tripService.update(eq(tripId), any(Trip.class))).thenReturn(updatedTrip);

        mockMvc.perform(put("/api/trips/{id}", tripId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrip)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Trip"));
    }

    @Test
    @DisplayName("PUT /api/trips/{id} - Access Denied")
    @WithMockUser(username = USER_EMAIL)
    void testUpdateTripAccessDenied() throws Exception {
        UUID tripId = UUID.randomUUID();
        Trip updateTrip = sampleTrip();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(false);

        mockMvc.perform(put("/api/trips/{id}", tripId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrip)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/trips/{id} - Success")
    @WithMockUser(username = USER_EMAIL)
    void testDeleteTripSuccess() throws Exception {
        UUID tripId = UUID.randomUUID();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(true);
        doNothing().when(tripService).delete(tripId);

        mockMvc.perform(delete("/api/trips/{id}", tripId).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/trips/{id} - Access Denied")
    @WithMockUser(username = USER_EMAIL)
    void testDeleteTripAccessDenied() throws Exception {
        UUID tripId = UUID.randomUUID();

        when(userService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(tripService.canUserAccessTrip(tripId, USER_ID)).thenReturn(false);

        mockMvc.perform(delete("/api/trips/{id}", tripId).with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
