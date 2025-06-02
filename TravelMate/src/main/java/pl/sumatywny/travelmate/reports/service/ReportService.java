package pl.sumatywny.travelmate.reports.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.participant.model.Participant;
import pl.sumatywny.travelmate.participant.repository.ParticipantRepository;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.entity.Note;
import pl.sumatywny.travelmate.reports.repository.NoteRepository;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Service
public class ReportService {
    NoteRepository noteRepository;
    TripService tripService;
    //ParticipantService participantService;
    ParticipantRepository participantRepository;//temporary

    @Autowired
    public ReportService(NoteRepository noteRepository, TripService tripService, ParticipantRepository participantRepository) {
        this.noteRepository = noteRepository;
        this.tripService = tripService;
        this.participantRepository = participantRepository;
    }




    public List<NoteDTO> getTripNotes(Long tripId) {
        Trip trip = tripService.findById(tripId);
        List<Note> notes = noteRepository.getNotesByTrip(trip).stream().sorted(Comparator.comparing(Note::getDate)).toList();
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

    public void addNote(String author, String content, Long tripId) {
        Trip trip = tripService.findById(tripId);
        Participant participant = participantRepository.getParticipantByEmail(author); // temporary
        LocalDate noteDate = LocalDate.now();
        Note newNote = new Note();
        newNote.setDate(noteDate);
        newNote.setContent(content);
        newNote.setAuthor(participant);
        newNote.setTrip(trip);
        noteRepository.save(newNote);
    }

    public void alterNote(Long noteId, String newContent) {
        Note alteredNote = noteRepository.getNoteById(noteId);
        alteredNote.setContent(newContent);
        alteredNote.setDate(LocalDate.now());
        noteRepository.save(alteredNote);
    }

}
