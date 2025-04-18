package pl.sumatywny.travelmate.points.dto;

import lombok.Getter;
import lombok.Setter;
import pl.sumatywny.travelmate.points.model.PointType;

@Getter
@Setter
public class SearchDto {
    private String city;
    private PointType pointType;
}
