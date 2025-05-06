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

}
