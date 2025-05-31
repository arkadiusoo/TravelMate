package pl.sumatywny.travelmate.reports.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.service.ReportService;

import java.util.List;

@RestController
public class ReportController {
    ReportService reportService;

    @GetMapping(value = "/notes/trip/{tripId}")
    public ResponseEntity<List<NoteDTO>> getTripNotes(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(reportService.getTripNotes(tripId));
    }

    @PostMapping(value = "/notes/add")
    public ResponseEntity<String> addNote(@RequestParam Long tripId, @RequestBody NoteDTO noteDTO) {
        reportService.addNote(noteDTO.getAuthor(), noteDTO.getContent(), tripId);
        return ResponseEntity.ok("Added note successfully");
    }

    @PostMapping(value = "/notes/alter")
    public ResponseEntity<String> alterNote(@RequestBody NoteDTO noteDTO) {
        reportService.alterNote(noteDTO.getId(), noteDTO.getContent());
        return ResponseEntity.ok("Added note successfully");
    }
}
