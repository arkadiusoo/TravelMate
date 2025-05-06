package pl.sumatywny.travelmate.trip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GooglePlacesService {

    @Value("${google.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create("https://maps.googleapis.com/maps/api");

    public String autocomplete(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/autocomplete/json")
                        .queryParam("input", query)
                        .queryParam("language", "pl")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getPlaceId(String place) {
        String json = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/autocomplete/json")
                        .queryParam("input", place)
                        .queryParam("language", "pl")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode predictions = root.path("predictions");
            if (predictions.isArray() && predictions.size() > 0) {
                return predictions.get(0).path("place_id").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getPlaceDetails(String placeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/details/json")
                        .queryParam("place_id", placeId)
                        .queryParam("fields", "geometry,name,formatted_address")
                        .queryParam("language", "pl")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
