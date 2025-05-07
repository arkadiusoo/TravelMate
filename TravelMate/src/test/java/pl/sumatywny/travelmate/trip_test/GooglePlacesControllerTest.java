package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.trip.controller.GooglePlacesController;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GooglePlacesController.class)
class GooglePlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GooglePlacesService googlePlacesService;

    @Test
    void shouldReturnSearchResults() throws Exception {
        String mockResponse = "{\"predictions\": [{\"description\": \"Kraków\", \"place_id\": \"abc123\"}]}";

        Mockito.when(googlePlacesService.search("Kraków")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/places/search")
                        .param("q", "Kraków"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    void shouldReturnPlaceDetails() throws Exception {
        String mockDetails = "{\"result\": {\"name\": \"Kraków\", \"formatted_address\": \"Kraków, Polska\"}}";

        Mockito.when(googlePlacesService.getPlaceDetails("abc123")).thenReturn(mockDetails);

        mockMvc.perform(get("/api/places/details")
                        .param("placeId", "abc123"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockDetails));
    }
}
