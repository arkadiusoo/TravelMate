package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.trip.controller.PointController;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Autowired
    private ObjectMapper objectMapper;

    private Point samplePoint() {
        Trip trip = new Trip();
        trip.setId(1L);

        Point point = new Point();
        point.setId(10L);
        point.setTitle("Test Point");
        point.setDescription("Some description");
        point.setLatitude(50.0);
        point.setLongitude(19.0);
        point.setDate(LocalDate.of(2025, 5, 7));
        point.setTrip(trip);

        return point;
    }

    @Test
    void shouldReturnAllPoints() throws Exception {
        Point point = samplePoint();

        Mockito.when(pointService.findByTripId(1L)).thenReturn(List.of(point));

        mockMvc.perform(get("/api/trips/1/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Point"));
    }

    @Test
    void shouldReturnOnePoint() throws Exception {
        Point point = samplePoint();

        Mockito.when(pointService.findById(1L, 10L)).thenReturn(point);

        mockMvc.perform(get("/api/trips/1/points/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Point"));
    }

    @Test
    void shouldCreatePoint() throws Exception {
        Point point = samplePoint();

        Mockito.when(pointService.create(eq(1L), any(Point.class))).thenReturn(point);

        mockMvc.perform(post("/api/trips/1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(point)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Test Point"));
    }

    @Test
    void shouldUpdatePoint() throws Exception {
        Point point = samplePoint();
        point.setTitle("Updated Title");

        Mockito.when(pointService.update(eq(1L), eq(10L), any(Point.class))).thenReturn(point);

        mockMvc.perform(put("/api/trips/1/points/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(point)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldDeletePoint() throws Exception {
        mockMvc.perform(delete("/api/trips/1/points/10"))
                .andExpect(status().isOk());

        Mockito.verify(pointService).delete(1L, 10L);
    }
}
