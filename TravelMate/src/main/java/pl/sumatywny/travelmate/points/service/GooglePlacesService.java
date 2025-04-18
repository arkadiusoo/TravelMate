package pl.sumatywny.travelmate.points.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.points.model.Point;
import pl.sumatywny.travelmate.points.model.PointType;

import java.util.ArrayList;
import java.util.List;

@Service
public class GooglePlacesService {
    private final GeoApiContext geoApiContext;

    public GooglePlacesService(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public List<Point> findAttractions(String city) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext,
                            city)
                    .type(PlaceType.TOURIST_ATTRACTION)
                    .await();
            List<Point> points = new ArrayList<>();
            for (PlacesSearchResult r : response.results) {
                PlaceDetails details = PlacesApi.placeDetails(geoApiContext, r.placeId).fields(
                        PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
                        PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                        PlaceDetailsRequest.FieldMask.OPENING_HOURS,
                        PlaceDetailsRequest.FieldMask.WEBSITE).await();
                Point point = new Point(r.placeId, r.rating, PointType.ATTRACTION, r.name, city, r.geometry.location.lat, r.geometry.location.lng, r.formattedAddress, details.formattedPhoneNumber, details.website, details.openingHours);
                points.add(point);
            }
            return points;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Point> findRestaurants(String city) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext,
                            city)
                    .type(PlaceType.RESTAURANT)
                    .await();
            List<Point> points = new ArrayList<>();
            for (PlacesSearchResult r : response.results) {
                PlaceDetails details = PlacesApi.placeDetails(geoApiContext, r.placeId).fields(
                        PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
                        PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                        PlaceDetailsRequest.FieldMask.OPENING_HOURS,
                        PlaceDetailsRequest.FieldMask.WEBSITE).await();
                Point point = new Point(r.placeId, r.rating, PointType.FOOD, r.name, city, r.geometry.location.lat, r.geometry.location.lng, r.formattedAddress, details.formattedPhoneNumber, details.website, details.openingHours);
                points.add(point);
            }
            return points;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Point> findLodging(String city) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext,
                            city)
                    .type(PlaceType.LODGING)
                    .await();
            List<Point> points = new ArrayList<>();
            for (PlacesSearchResult r : response.results) {
                PlaceDetails details = PlacesApi.placeDetails(geoApiContext, r.placeId).fields(
                        PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
                        PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
                        PlaceDetailsRequest.FieldMask.OPENING_HOURS,
                        PlaceDetailsRequest.FieldMask.WEBSITE).await();
                Point point = new Point(r.placeId, r.rating, PointType.ACCOMODATION, r.name, city, r.geometry.location.lat, r.geometry.location.lng, r.formattedAddress, details.formattedPhoneNumber, details.website, details.openingHours);
                points.add(point);
            }
            return points;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
