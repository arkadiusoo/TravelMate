package pl.sumatywny.travelmate.trip;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
public class OpenAiService {

    private final GooglePlacesService googlePlacesService;

    @Value("${openai.api.key}")
    private String apiKey;

    String systemPrompt = """
Jesteś osobą, która generuje plan wycieczki i zwiedzania danego miasta. 
Użytkownik przekazuje ci miasto które go interesuje oraz daty, w których ma zaplanowany pobyt. 
Twoim zadaniem jest zaplanowanie całej wycieczki jeśli chodzi o atrakcje turystyczne.

Ważne:
- Kolejne atrakcje tego samego dnia muszą być po drodze, aby nie nadrabiać kilometrów.
- Atrakcje muszą być realne czasowo — nie może być ich za dużo na jeden dzień.
- Bierz od najpopularniejszych i najfajniejszych atrakcji i miejsc wartych zobaczenia w danym mieście.
- Odpowiedź MUSI być w poniższym formacie (i tylko w nim!):
- Zwracaj z dopiskiem w jakim mieście
- Na pierwszy i ostatni dzień wycieczki dawaj 2 atrakcje podczas gdy na środkowe dni 3/4/5 atrakcji w zależności od twojej wiedzy o czasie zwiedzania

[
  {
    "Data odwiedzin": "08.05.2025",
    "Nazwa miejsca": "Pałac Prezydencki w Warszawie"
  },
  {
    "Data odwiedzin": "08.05.2025",
    "Nazwa miejsca": "Zamek Królewski w Warszawie"
  },
  {
    "Data odwiedzin": "09.05.2025",
    "Nazwa miejsca": "Stadion Narodowy w Warszawie"
  }
]

NIE CHCĘ NIC INNEGO W ODPOWIEDZI.
""";

    public OpenAiService(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }


    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
            .build();

    public List<PlaceVisitDto> askChatGpt(String userPrompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));
        messages.add(Map.of(
                "role", "user",
                "content", userPrompt
        ));

        body.put("messages", messages);

        String response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String contentJsonString = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        List<Map<String, Object>> result;
        try {
            result = objectMapper.readValue(contentJsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        List<PlaceVisitDto> details = getDetails(result);
        return details;
    }

    public List<PlaceVisitDto> getDetails(List<Map<String, Object>> locations) {
        List<PlaceVisitDto> result = new ArrayList<>();

        for (Map<String, Object> loc : locations) {
            String placeName = (String) loc.get("Nazwa miejsca");
            String visitDate = (String) loc.get("Data odwiedzin");

            String placeId = googlePlacesService.getPlaceId(placeName);
            String detailsJson = googlePlacesService.getPlaceDetails(placeId);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(detailsJson);
                JsonNode resultNode = root.path("result");
                JsonNode location = resultNode.path("geometry").path("location");

                PlaceVisitDto dto = new PlaceVisitDto();
                dto.setName(resultNode.path("name").asText());
                dto.setAddress(resultNode.path("formatted_address").asText());
                dto.setLat(location.path("lat").asDouble());
                dto.setLng(location.path("lng").asDouble());
                dto.setDate(visitDate);

                result.add(dto);

            } catch (Exception e) {
                e.printStackTrace(); // lub log
            }
        }
        return result;
    }


}
