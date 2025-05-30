package pl.sumatywny.travelmate.reports.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import pl.sumatywny.travelmate.participant.model.Participant;

@Setter
@Getter
@Entity
public class Note {
    @Id
    private Long id;

    private String content;

    @ManyToOne
    private Participant author;

}
