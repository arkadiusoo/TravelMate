package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.service.OpenAiService;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Assistant", description = "Interact with OpenAI to generate place visit suggestions")
public class ChatController {

    private final OpenAiService openAiService;
    private final PointService pointService;

    @Autowired
    public ChatController(OpenAiService openAiService, PointService pointService) {
        this.openAiService = openAiService;
        this.pointService = pointService;
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
        for (PlaceVisitDto placeVisitDto : response) {
            Point point = Point.builder()
                    .title(placeVisitDto.getName())
                    .date(LocalDate.parse(placeVisitDto.getDate()))
                    .description(placeVisitDto.getAddress())
                    .latitude(placeVisitDto.getLat())
                    .longitude(placeVisitDto.getLng())
                    .visited(false).build();
            pointService.create(request.getTripId(), point);
        }
        return ResponseEntity.ok(response);
    }
}
