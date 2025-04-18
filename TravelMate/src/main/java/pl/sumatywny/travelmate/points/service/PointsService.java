package pl.sumatywny.travelmate.points.service;

import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.points.model.Point;
import pl.sumatywny.travelmate.points.model.PointType;

import java.util.List;

@Service
public class PointsService {
    private final GooglePlacesService googlePlacesService;

    public PointsService(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
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
}
