package pl.sumatywny.travelmate.trip.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.reports.dto.NoteDTO;
import pl.sumatywny.travelmate.reports.service.ReportService;
import pl.sumatywny.travelmate.trip.dto.ChatNoteRequestDto;
import pl.sumatywny.travelmate.trip.dto.ChatRequestDto;
import pl.sumatywny.travelmate.trip.dto.PlaceVisitDto;
import pl.sumatywny.travelmate.trip.model.Point;
import pl.sumatywny.travelmate.trip.model.Trip;
import pl.sumatywny.travelmate.trip.service.OpenAiService;
import pl.sumatywny.travelmate.trip.service.PointService;
import pl.sumatywny.travelmate.trip.service.TripService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Assistant", description = "Interact with OpenAI to generate place visit suggestions")
public class ChatController {

    private final OpenAiService openAiService;
    private final PointService pointService;
    private final ReportService reportService;
    private final TripService tripService;

    @Autowired
    public ChatController(OpenAiService openAiService, PointService pointService, ReportService reportService, TripService tripService) {
        this.openAiService = openAiService;
        this.pointService = pointService;
        this.reportService = reportService;
        this.tripService = tripService;
    }

    @Operation(
            summary = "Ask the AI for travel recommendations",
            description = "Sends a user prompt to the OpenAI API and receives a list of recommended places to visit.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of recommended places returned successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaceVisitDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<List<PlaceVisitDto>> ask(@RequestBody ChatRequestDto request) {
        List<PlaceVisitDto> response = openAiService.askChatGpt(request.getPrompt());
        for (PlaceVisitDto placeVisitDto : response) {
            Point point = Point.builder()
                    .title(placeVisitDto.getName())
                    .date(LocalDate.parse(placeVisitDto.getDate()))
                    .description(placeVisitDto.getAddress())
                    .latitude(placeVisitDto.getLat())
                    .longitude(placeVisitDto.getLng())
                    .visited(false).build();
            pointService.create(request.getTripId(), point);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/note")
    public ResponseEntity<byte[]> generateSummerizarOfNotes(@RequestBody ChatNoteRequestDto request) throws IOException {
        List<NoteDTO> notes = reportService.getTripNotes(request.getTripId());
        List<Point> points = pointService.findByTripId(request.getTripId());
        List<String> descriptions = new ArrayList<>();

        for (NoteDTO noteDTO : notes) {
            String pointName = noteDTO.getPointName();
            Point matchingPoint = points.stream()
                    .filter(p -> p.getTitle().equals(pointName))
                    .findFirst()
                    .orElse(null);
            if (matchingPoint != null) {
                String date = matchingPoint.getDate().toString();
                descriptions.add("Data odwiedzin: " + date +
                        " | Notatka o " + pointName + ": " + noteDTO.getContent());
            } else {
                descriptions.add("Notatka o " + pointName + ": " + noteDTO.getContent());
            }
        }

        String userPrompt = String.join("\n", descriptions);
        Trip trip = tripService.findById(request.getTripId());
        String tripName = trip.getName();
        System.out.println(userPrompt);
        String response = openAiService.askChatGptNote(userPrompt);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        try {
            Image logo = Image.getInstance(getClass().getResource("/static/logo.png"));
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
        }

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Raport z wycieczki - " + tripName , titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        Font textFont = new Font(Font.HELVETICA, 12);
        Paragraph content = new Paragraph(response, textFont);
        content.setAlignment(Element.ALIGN_LEFT);
        content.setLeading(16f);
        document.add(content);

        document.close();

        byte[] pdfBytes = out.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("raport-wycieczka.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
