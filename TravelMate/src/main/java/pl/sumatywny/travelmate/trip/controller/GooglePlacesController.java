package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;

@RestController
@RequestMapping("/api/places")
@Tag(name = "Google Places", description = "Search and retrieve details from Google Places API")
public class GooglePlacesController {

    private final GooglePlacesService googlePlacesService;

    public GooglePlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Operation(
            summary = "Search for places",
            description = "Returns a list of place suggestions based on the given query string.",
            parameters = {
                    @Parameter(name = "q", description = "Search query, e.g., name or type of place", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Places found successfully",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Invalid search query", content = @Content)
            }
    )
    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return googlePlacesService.search(q);
    }

    @Operation(
            summary = "Get place details",
            description = "Returns detailed information about a specific place by its Google place ID.",
            parameters = {
                    @Parameter(name = "placeId", description = "Google Place ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Place details retrieved successfully",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Place not found", content = @Content)
            }
    )
    @GetMapping("/details")
    public String details(@RequestParam String placeId) {
        return googlePlacesService.getPlaceDetails(placeId);
    }
}
