package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.service.PointService;
import pl.sumatywny.travelmate.trip.service.TripService;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.security.model.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/points")
@Tag(name = "Points", description = "Manage travel points within a specific trip")
public class PointController {

    private final PointService pointService;
    private final TripService tripService;
    private final UserService userService;

    public PointController(PointService pointService, TripService tripService, UserService userService) {
        this.pointService = pointService;
        this.tripService = tripService;
        this.userService = userService;
    }

    @Operation(
            summary = "Get all points for a trip",
            description = "Returns a list of all points associated with a specific trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of points returned successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @GetMapping
    public List<Point> getAll(@PathVariable UUID tripId, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return pointService.findByTripId(tripId);
    }

    @Operation(
            summary = "Create a new point in a trip",
            description = "Adds a new point to the specified trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point created successfully",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @PostMapping
    public Point create(@PathVariable UUID tripId, @RequestBody Point point, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return pointService.create(tripId, point);
    }

    @Operation(
            summary = "Get a specific point from a trip",
            description = "Fetches a single point by its ID and associated trip ID. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point found",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public Point getOne(@PathVariable UUID tripId, @PathVariable Long id, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return pointService.findById(tripId, id);
    }

    @Operation(
            summary = "Update a point in a trip",
            description = "Updates the details of a specific point in the given trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point updated successfully",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public Point update(@PathVariable UUID tripId, @PathVariable Long id, @RequestBody Point point, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return pointService.update(tripId, id, point);
    }

    @Operation(
            summary = "Delete a point from a trip",
            description = "Removes a specific point from the trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Point deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID tripId, @PathVariable Long id, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        pointService.delete(tripId, id);
    }

    @Operation(
            summary = "Change a visited status",
            description = "Changes a visited status for specific point from the trip. User must be a participant.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Point status successfully changed"),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @PatchMapping("/{id}/visited")
    public Point markVisited(@PathVariable UUID tripId, @PathVariable Long id, Authentication authentication) {
        UUID currentUserId = extractUserIdFromAuthentication(authentication);

        if (!tripService.canUserAccessTrip(tripId, currentUserId)) {
            throw new RuntimeException("Access denied: You are not a participant in this trip");
        }

        return pointService.markVisited(tripId, id);
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user.getId();
    }
}