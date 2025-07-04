package pl.sumatywny.travelmate.trip_test;

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
import pl.sumatywny.travelmate.security.service.JwtService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.trip.controller.GooglePlacesController;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(GooglePlacesController.class)
class GooglePlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GooglePlacesService googlePlacesService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter)
                .doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturnSearchResults() throws Exception {
        String query = "Wawel";
        String mockResponse = """
            {
              "predictions": [
                {
                  "description": "Wawel, Kraków",
                  "place_id": "abc123"
                }
              ],
              "status": "OK"
            }
            """;

        when(googlePlacesService.search(query)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/places/search")
                        .param("q", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponse));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturnPlaceDetails() throws Exception {
        String placeId = "abc123";
        String mockDetails = """
            {
              "result": {
                "name": "Wawel",
                "formatted_address": "Kraków, Polska",
                "geometry": {
                  "location": {
                    "lat": 50.054,
                    "lng": 19.936
                  }
                }
              }
            }
            """;

        when(googlePlacesService.getPlaceDetails(placeId)).thenReturn(mockDetails);

        mockMvc.perform(get("/api/places/details")
                        .param("placeId", placeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mockDetails));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturnBadRequestWhenMissingSearchQuery() throws Exception {
        mockMvc.perform(get("/api/places/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturnBadRequestWhenMissingPlaceId() throws Exception {
        mockMvc.perform(get("/api/places/details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
