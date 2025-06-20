package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
import pl.sumatywny.travelmate.trip.service.OpenAiService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Assistant", description = "Interact with OpenAI to generate place visit suggestions")
public class ChatController {

    private final OpenAiService openAiService;

    @Autowired
    public ChatController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @Operation(
            summary = "Ask the AI for travel recommendations",
            description = "Sends a user prompt to the OpenAI API and receives a list of recommended places to visit.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of recommended places returned successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaceVisitDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<List<PlaceVisitDto>> ask(@RequestBody ChatRequestDto request) {
        List<PlaceVisitDto> response = openAiService.askChatGpt(request.getPrompt());
        return ResponseEntity.ok(response);
    }
}
