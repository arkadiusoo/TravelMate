package pl.sumatywny.travelmate.trip;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping
    public List<Point> getAll(@PathVariable Long tripId) {
        return pointService.findByTripId(tripId);
    }

    @PostMapping
    public Point create(@PathVariable Long tripId, @RequestBody Point point) {
        return pointService.create(tripId, point);
    }

    @GetMapping("/{id}")
    public Point getOne(@PathVariable Long tripId, @PathVariable Long id) {
        return pointService.findById(tripId, id);
    }

    @PutMapping("/{id}")
    public Point update(@PathVariable Long tripId, @PathVariable Long id, @RequestBody Point point) {
        return pointService.update(tripId, id, point);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long tripId, @PathVariable Long id) {
        pointService.delete(tripId, id);
    }
}
