package pl.sumatywny.travelmate.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.sumatywny.travelmate.reports.entity.Note;
import pl.sumatywny.travelmate.trip.model.Trip;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> getNotesByTrip(Trip trip);

    Note getNoteById(Long noteId);
}
