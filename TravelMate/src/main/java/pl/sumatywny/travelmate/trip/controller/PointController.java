package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.service.PointService;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/points")
@Tag(name = "Points", description = "Manage travel points within a specific trip")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @Operation(
            summary = "Get all points for a trip",
            description = "Returns a list of all points associated with a specific trip.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of points returned successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Point.class)))
            }
    )
    @GetMapping
    public List<Point> getAll(@PathVariable Long tripId) {
        return pointService.findByTripId(tripId);
    }

    @Operation(
            summary = "Create a new point in a trip",
            description = "Adds a new point to the specified trip.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point created successfully",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping
    public Point create(@PathVariable Long tripId, @RequestBody Point point) {
        return pointService.create(tripId, point);
    }

    @Operation(
            summary = "Get a specific point from a trip",
            description = "Fetches a single point by its ID and associated trip ID.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point found",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public Point getOne(@PathVariable Long tripId, @PathVariable Long id) {
        return pointService.findById(tripId, id);
    }

    @Operation(
            summary = "Update a point in a trip",
            description = "Updates the details of a specific point in the given trip.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Point updated successfully",
                            content = @Content(schema = @Schema(implementation = Point.class))),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public Point update(@PathVariable Long tripId, @PathVariable Long id, @RequestBody Point point) {
        return pointService.update(tripId, id, point);
    }

    @Operation(
            summary = "Delete a point from a trip",
            description = "Removes a specific point from the trip.",
            parameters = {
                    @Parameter(name = "tripId", description = "ID of the trip"),
                    @Parameter(name = "id", description = "ID of the point")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Point deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Point not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long tripId, @PathVariable Long id) {
        pointService.delete(tripId, id);
    }
}
