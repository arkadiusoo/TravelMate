package pl.sumatywny.travelmate.trip_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;

import static org.assertj.core.api.Assertions.assertThat;

class GooglePlacesServiceTest {

    private MockWebServer mockWebServer;
    private GooglePlacesService googlePlacesService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        googlePlacesService = new GooglePlacesService();

        WebClient testClient = WebClient.create(mockWebServer.url("/maps/api").toString());
        ReflectionTestUtils.setField(googlePlacesService, "webClient", testClient);
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "FAKE_API_KEY");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnPlaceIdFromAutocomplete() {
        String mockResponse = """
            {
              "predictions": [
                {
                  "description": "Wawel, Kraków",
                  "place_id": "test_place_id"
                }
              ],
              "status": "OK"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String placeId = googlePlacesService.getPlaceId("Wawel");

        assertThat(placeId).isEqualTo("test_place_id");
    }

    @Test
    void shouldReturnNullWhenNoPredictions() {
        String mockResponse = """
            {
              "predictions": [],
              "status": "ZERO_RESULTS"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String placeId = googlePlacesService.getPlaceId("Nieznane miejsce");

        assertThat(placeId).isNull();
    }

    @Test
    void shouldReturnPlaceDetailsJson() {
        String mockResponse = """
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

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String json = googlePlacesService.getPlaceDetails("test_place_id");

        assertThat(json).contains("Wawel");
        assertThat(json).contains("Kraków");
        assertThat(json).contains("50.054");
    }

    @Test
    void shouldReturnRawSearchJson() {
        String mockResponse = """
            {
              "predictions": [
                {
                  "description": "Wawel, Kraków",
                  "place_id": "test_place_id"
                }
              ]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String json = googlePlacesService.search("Wawel");

        assertThat(json).contains("place_id");
    }
}
