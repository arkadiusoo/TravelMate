package pl.sumatywny.travelmate.trip.controller;

import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<Trip> getAll() {
        return tripService.findAll();
    }

    @PostMapping
    public Trip create(@RequestBody Trip trip) {
        return tripService.create(trip);
    }

    @GetMapping("/{id}")
    public Trip getOne(@PathVariable Long id) {
        return tripService.findById(id);
    }

    @PutMapping("/{id}")
    public Trip update(@PathVariable Long id, @RequestBody Trip trip) {
        return tripService.update(id, trip);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tripService.delete(id);
    }
}
