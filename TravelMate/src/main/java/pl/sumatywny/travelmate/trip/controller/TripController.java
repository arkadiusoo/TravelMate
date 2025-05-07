package pl.sumatywny.travelmate.trip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trips", description = "Manage user trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @Operation(
            summary = "Get all trips",
            description = "Retrieves a list of all created trips.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of trips retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Trip.class)))
            }
    )
    @GetMapping
    public List<Trip> getAll() {
        return tripService.findAll();
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
    public Trip create(@RequestBody Trip trip) {
        return tripService.create(trip);
    }

    @Operation(
            summary = "Get a specific trip",
            description = "Retrieves a trip by its unique identifier.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trip found",
                            content = @Content(schema = @Schema(implementation = Trip.class))),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public Trip getOne(@PathVariable Long id) {
        return tripService.findById(id);
    }

    @Operation(
            summary = "Update a trip",
            description = "Updates the details of an existing trip.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip to update")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Trip updated successfully",
                            content = @Content(schema = @Schema(implementation = Trip.class))),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public Trip update(@PathVariable Long id, @RequestBody Trip trip) {
        return tripService.update(id, trip);
    }

    @Operation(
            summary = "Delete a trip",
            description = "Deletes a specific trip by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the trip to delete")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Trip deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tripService.delete(id);
    }
}
