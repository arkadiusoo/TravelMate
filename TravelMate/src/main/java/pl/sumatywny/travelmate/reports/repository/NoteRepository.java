package pl.sumatywny.travelmate.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.sumatywny.travelmate.reports.entity.Note;
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
}
