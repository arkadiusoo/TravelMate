package pl.sumatywny.travelmate.points.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.points.dto.SearchDto;
import pl.sumatywny.travelmate.points.model.Point;
import pl.sumatywny.travelmate.points.service.PointsService;

import java.util.List;

@RestController
@RequestMapping("/trips/{tripId}")
@RequiredArgsConstructor
@Tag(name = "Trips points", description = "Operations related to places - searching, adding, getting")
public class PointsController {

    private final PointsService pointsService;

    @Operation(
            summary = "Search places",
            description = "Returns a list of places - attractions, restaurants, lodging"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Places retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Places not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/points/search")
    public ResponseEntity<List<Point>> searchPlaces(
            @Parameter(description = "Trip Id", required = true)
            @PathVariable String tripId,
            @Valid
            @RequestBody SearchDto search
    ) {
        List<Point> places = pointsService.searchPoint(search.getPointType(), search.getCity());
        return ResponseEntity.ok(places);
    }

    @Operation(
            summary = "Get all points for trip",
            description = "Retrieves all points saved for the specified trip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Places retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Places not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/points")
    public ResponseEntity<List<Point>> getPointsForTrip(
            @Parameter(description = "Trip ID", required = true)
            @PathVariable int tripId
    ) {
        List<Point> points = pointsService.getPointsByTripId(tripId);
        return ResponseEntity.ok(points);
    }

    @Operation(
            summary = "Add point to trip",
            description = "Adds a selected place to the specified trip and segment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Place added successfully"),
            @ApiResponse(responseCode = "404", description = "Place added unsuccessfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/segments/{segmentId}/points")
    public ResponseEntity<Point> addPointToTrip(
            @Parameter(description = "Trip ID", required = true)
            @PathVariable int tripId,
            @Parameter(description = "Segment ID", required = true)
            @PathVariable int segmentId,
            @Valid @RequestBody Point point
    ) {
        Point saved = pointsService.addPointToTrip(tripId, segmentId, point);
        return ResponseEntity.status(201).body(saved);
    }
}