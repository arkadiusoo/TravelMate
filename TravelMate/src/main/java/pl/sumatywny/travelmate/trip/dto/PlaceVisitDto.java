package pl.sumatywny.travelmate.trip.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceVisitDto {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String date;

    public PlaceVisitDto(String name, String address, double lat, double lng, String date) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public PlaceVisitDto() {}
}
