package pl.sumatywny.travelmate.reports.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.web.WebProperties;
import pl.sumatywny.travelmate.participant.model.Participant;
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

}
