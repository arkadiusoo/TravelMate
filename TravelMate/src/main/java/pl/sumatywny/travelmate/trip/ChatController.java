package pl.sumatywny.travelmate.trip;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OpenAiService openAiService;

    public ChatController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping
    public ResponseEntity<List<PlaceVisitDto>> ask(@RequestBody ChatRequest request) {
        List<PlaceVisitDto> response = openAiService.askChatGpt(request.getPrompt());
        return ResponseEntity.ok(response);
    }

    @Getter
    public static class ChatRequest {
        private String prompt;
    }
}
