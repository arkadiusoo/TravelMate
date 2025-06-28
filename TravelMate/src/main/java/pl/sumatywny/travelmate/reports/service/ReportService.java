package pl.sumatywny.travelmate.reports.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.entity.Note;
import pl.sumatywny.travelmate.reports.repository.NoteRepository;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.repository.PointRepository;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    NoteRepository noteRepository;
    TripService tripService;
    ParticipantRepository participantRepository;//temporary
    PointRepository pointRepository;

    @Autowired
    public ReportService(NoteRepository noteRepository,
                         TripService tripService,
                         ParticipantRepository participantRepository,
                         PointRepository pointRepository) {
        this.noteRepository = noteRepository;
        this.tripService = tripService;
        this.participantRepository = participantRepository;
        this.pointRepository = pointRepository;
    }

    public List<NoteDTO> getTripNotes(UUID tripId) {
        Trip trip = tripService.findById(tripId);
        List<Note> notes = noteRepository.getNotesByTrip(trip).stream().sorted(Comparator.comparing(Note::getDate)).toList();
        return getNoteDTOS(notes);
    }

    public List<NoteDTO> getPointNotes(Long pointId) {
        Point point = pointRepository.getPointById(pointId);
        List<Note> notes = noteRepository.getNotesByPoint(point).stream().sorted(Comparator.comparing(Note::getDate)).toList();
        return getNoteDTOS(notes);
    }

    private List<NoteDTO> getNoteDTOS(List<Note> notes) {
        List<NoteDTO> noteDTOs = new LinkedList<>();
        for (Note note : notes) {
            NoteDTO noteDTO = NoteDTO.builder()
                    .id(note.getId())
                    .date(note.getDate().atStartOfDay())
                    .author(note.getAuthor().getEmail())
                    .content(note.getContent())
                    .build();
            noteDTOs.add(noteDTO);
        }
        return noteDTOs;
    }

    public void addNote(String author, String content, UUID tripId, UUID pointId) {
        Trip trip = tripService.findById(tripId);
        Participant participant = participantRepository.getParticipantByEmail(author); // temporary
        LocalDate noteDate = LocalDate.now();
        Note newNote = new Note();
        newNote.setDate(noteDate);
        newNote.setContent(content);
        newNote.setAuthor(participant);
        newNote.setTrip(trip);
        newNote.setId(pointId);
        noteRepository.save(newNote);
    }

    public void alterNote(UUID noteId, String newContent) {
        Note alteredNote = noteRepository.getNoteById(noteId);
        alteredNote.setContent(newContent);
        alteredNote.setDate(LocalDate.now());
        noteRepository.save(alteredNote);
    }

}
