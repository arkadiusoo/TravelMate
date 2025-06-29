package pl.sumatywny.travelmate.reports.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
public class Note {
    @Id
    @GeneratedValue()
    private UUID id;

    private String content;

    private LocalDate date;

    @ManyToOne
    private Participant author;

    @ManyToOne
    private Trip trip;
    @ManyToOne
    private Point point;

}
