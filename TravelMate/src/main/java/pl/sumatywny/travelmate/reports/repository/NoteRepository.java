package pl.sumatywny.travelmate.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.sumatywny.travelmate.reports.entity.Note;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> getNotesByTrip(Trip trip);

    Note getNoteById(UUID noteId);

    List<Note> getNotesByPoint(Point point);
}
