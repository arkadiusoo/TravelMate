package pl.sumatywny.travelmate.points.service;

import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.points.model.Point;
import pl.sumatywny.travelmate.points.model.PointType;
import pl.sumatywny.travelmate.points.repository.PointRepository;

import java.util.List;

@Service
public class PointsService {
    private final GooglePlacesService googlePlacesService;
    private final PointRepository pointRepository;

    public PointsService(GooglePlacesService googlePlacesService, PointRepository pointRepository) {
        this.googlePlacesService = googlePlacesService;
        this.pointRepository = pointRepository;
    }

    public List<Point> searchPoint(PointType pointType, String city) {
        List<Point> points;
        if (pointType == PointType.ATTRACTION) {
            points = googlePlacesService.findAttractions(city);
        }
        else if (pointType == PointType.FOOD) {
            points = googlePlacesService.findRestaurants(city);
        }
        else {
            points = googlePlacesService.findLodging(city);
        }
        return points;
    }

    public Point addPointToTrip(long tripId, long segmentId, Point point) {
        point.setTripId(tripId);
        point.setSegmentId(segmentId);
        return pointRepository.save(point);
    }

    public List<Point> getPointsByTripId(long tripId) {
        return pointRepository.readPointsByTripId(tripId);
    }
}
