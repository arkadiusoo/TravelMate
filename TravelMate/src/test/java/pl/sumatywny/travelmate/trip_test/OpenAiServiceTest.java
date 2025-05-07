package pl.sumatywny.travelmate.trip_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;
import pl.sumatywny.travelmate.trip.service.OpenAiService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OpenAiServiceTest {

    private GooglePlacesService googlePlacesService;
    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        googlePlacesService = mock(GooglePlacesService.class);
        openAiService = new OpenAiService(googlePlacesService);
    }

    @Test
    void shouldReturnDetailsForPlace() throws Exception {
        List<Map<String, Object>> gptOutput = List.of(
                Map.of(
                        "Nazwa miejsca", "Wawel",
                        "Data odwiedzin", "2025-05-08"
                )
        );

        String fakePlaceId = "abc123";
        String fakePlaceDetailsJson = """
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

        when(googlePlacesService.getPlaceId("Wawel")).thenReturn(fakePlaceId);
        when(googlePlacesService.getPlaceDetails(fakePlaceId)).thenReturn(fakePlaceDetailsJson);

        List<PlaceVisitDto> result = openAiService.getDetails(gptOutput);

        assertThat(result).hasSize(1);
        PlaceVisitDto dto = result.get(0);
        assertThat(dto.getName()).isEqualTo("Wawel");
        assertThat(dto.getAddress()).isEqualTo("Kraków, Polska");
        assertThat(dto.getDate()).isEqualTo("2025-05-08");
        assertThat(dto.getLat()).isEqualTo(50.054);
        assertThat(dto.getLng()).isEqualTo(19.936);
    }
}
