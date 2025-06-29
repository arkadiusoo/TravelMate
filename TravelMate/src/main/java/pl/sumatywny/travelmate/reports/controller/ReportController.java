package pl.sumatywny.travelmate.reports.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.service.ReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReportController {
    ReportService reportService;
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/notes/trip/{tripId}")
    public ResponseEntity<List<NoteDTO>> getTripNotes(@PathVariable("tripId") UUID tripId) {
        return ResponseEntity.ok(reportService.getTripNotes(tripId));
    }

    @GetMapping(value = "/notes/point/{pointId}")
    public ResponseEntity<List<NoteDTO>> getPointNotes(@PathVariable("pointId") Long pointId) {
        return ResponseEntity.ok(reportService.getPointNotes(pointId));
    }

    @PostMapping(value = "/notes/add")
    public ResponseEntity<String> addNote(@RequestParam UUID tripId, @RequestBody NoteDTO noteDTO) {
        reportService.addNote(noteDTO.getAuthor(), noteDTO.getContent(), tripId, 1L);
        return ResponseEntity.ok("Added note successfully");
    }

    @PostMapping(value = "/notes/alter")
    public ResponseEntity<String> alterNote(@RequestBody NoteDTO noteDTO) {
        reportService.alterNote(noteDTO.getId(), noteDTO.getContent());
        return ResponseEntity.ok("Modified note successfully");
    }
}
