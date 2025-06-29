//package pl.sumatywny.travelmate.trip_test;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import pl.sumatywny.travelmate.trip.controller.TripController;
//import pl.sumatywny.travelmate.trip.model.Trip;
//import pl.sumatywny.travelmate.trip.service.TripService;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(TripController.class)
//class TripControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private TripService tripService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private Trip sampleTrip() {
//        Trip trip = new Trip();
//        trip.setId(1L);
//        trip.setName("Vacation");
//        trip.setStartDate(LocalDate.of(2025, 7, 1));
//        trip.setEndDate(LocalDate.of(2025, 7, 15));
//        return trip;
//    }
//
//    @Test
//    void shouldReturnAllTrips() throws Exception {
//        Trip trip = sampleTrip();
//
//        Mockito.when(tripService.findAll()).thenReturn(List.of(trip));
//
//        mockMvc.perform(get("/api/trips"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].name").value("Vacation"));
//    }
//
//    @Test
//    void shouldReturnOneTrip() throws Exception {
//        Trip trip = sampleTrip();
//
//        Mockito.when(tripService.findById(1L)).thenReturn(trip);
//
//        mockMvc.perform(get("/api/trips/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.name").value("Vacation"));
//    }
//
//    @Test
//    void shouldCreateTrip() throws Exception {
//        Trip trip = sampleTrip();
//
//        Mockito.when(tripService.create(any(Trip.class))).thenReturn(trip);
//
//        mockMvc.perform(post("/api/trips")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(trip)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.name").value("Vacation"));
//    }
//
//    @Test
//    void shouldUpdateTrip() throws Exception {
//        Trip updated = sampleTrip();
//        updated.setName("Updated");
//
//        Mockito.when(tripService.update(eq(1L), any(Trip.class))).thenReturn(updated);
//
//        mockMvc.perform(put("/api/trips/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updated)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Updated"));
//    }
//
//    @Test
//    void shouldDeleteTrip() throws Exception {
//        mockMvc.perform(delete("/api/trips/1"))
//                .andExpect(status().isOk());
//
//        Mockito.verify(tripService).delete(1L);
//    }
//}
