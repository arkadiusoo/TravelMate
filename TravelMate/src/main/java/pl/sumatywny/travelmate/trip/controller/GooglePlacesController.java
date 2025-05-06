package pl.sumatywny.travelmate.trip.controller;

import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.trip.service.GooglePlacesService;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

    private final GooglePlacesService googlePlacesService;

    public GooglePlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return googlePlacesService.search(q);
    }

    @GetMapping("/details")
    public String details(@RequestParam String placeId) {
        return googlePlacesService.getPlaceDetails(placeId);
    }
}
