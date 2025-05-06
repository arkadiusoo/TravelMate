package pl.sumatywny.travelmate.trip.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
import pl.sumatywny.travelmate.trip.service.OpenAiService;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OpenAiService openAiService;

    public ChatController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping
    public ResponseEntity<List<PlaceVisitDto>> ask(@RequestBody ChatRequestDto request) {
        List<PlaceVisitDto> response = openAiService.askChatGpt(request.getPrompt());
        return ResponseEntity.ok(response);
    }
}
