package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.security.model.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trips", description = "Manage user trips")
public class TripController {

    private final TripService tripService;
    private final UserService userService;

    public TripController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    @Operation(
            summary = "Get user's trips",
            description = "Retrieves a list of trips where the current user is a participant.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of user's trips retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Trip.class)))
            }
    )
    @GetMapping
    public List<Trip> getAll(Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);
        return tripService.findTripsByUserId(currentUserId);
    }

    @Operation(
            summary = "Create a new trip",
            description = "Creates a new trip with the provided information.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trip created successfully",
                            content = @Content(schema = @Schema(implementation = Trip.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping
    public Trip create(@RequestBody Trip trip, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);
        return tripService.create(trip, currentUserId);
    }

    @Operation(
            summary = "Get a specific trip",
            description = "Retrieves a trip by its unique identifier. User must be a participant.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trip found",
                            content = @Content(schema = @Schema(implementation = Trip.class))),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public Trip getOne(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(id, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return tripService.findById(id);
    }

    @Operation(
            summary = "Update a trip",
            description = "Updates the details of an existing trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip to update")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trip updated successfully",
                            content = @Content(schema = @Schema(implementation = Trip.class))),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public Trip update(@PathVariable UUID id, @RequestBody Trip trip, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(id, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return tripService.update(id, trip);
    }

    @Operation(
            summary = "Delete a trip",
            description = "Deletes a specific trip by its ID. User must be a participant.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip to delete")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Trip deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(id, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        tripService.delete(id);
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user.getId();
    }
}