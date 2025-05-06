package pl.sumatywny.travelmate.trip;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

    private final GooglePlacesService googlePlacesService;

    public GooglePlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @GetMapping("/autocomplete")
    public String autocomplete(@RequestParam String q) {
        return googlePlacesService.autocomplete(q);
    }

    @GetMapping("/details")
    public String details(@RequestParam String placeId) {
        return googlePlacesService.getPlaceDetails(placeId);
    }
}
