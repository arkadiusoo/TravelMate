package pl.sumatywny.travelmate.points.model;

import com.google.maps.model.OpeningHours;
import jakarta.persistence.*;
import lombok.Getter;

import java.net.URL;

@Entity
@Table(name="Points")
public class Point {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private int id;

    @Getter
    @Column(name = "placeId", nullable = false)
    private String placeId;

    @Getter
    @Column(name = "rating")
    private float rating;

    @Getter
    @Column(name = "type", nullable = false)
    private PointType type;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "city", nullable = false)
    private String city;

    @Getter
    @Column(name = "lat", nullable = false)
    private double lat;

    @Getter
    @Column(name = "lon", nullable = false)
    private double lon;

    @Getter
    @Column(name = "fullAddress", nullable = false)
    private String fullAddress;

    @Getter
    @Column(name = "phone")
    private String phone;

    @Getter
    @Column(name = "website")
    private URL website;

    @Getter
    @Column(name = "hours")
    private OpeningHours hours;

    public Point(String placeId, float rating, PointType type, String name, String city, double lat, double lon, String fullAddress, String phone, URL website, OpeningHours hours) {
        this.placeId = placeId;
        this.rating = rating;
        this.type = type;
        this.name = name;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
        this.fullAddress = fullAddress;
        this.phone = phone;
        this.website = website;
        this.hours = hours;
    }

    public Point() {

    }
}
