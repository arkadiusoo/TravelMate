package pl.sumatywny.travelmate.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single Note")

public class NoteDTO {
    private UUID id;

    @Schema(
            description = "Date of Note"
    )
    private LocalDateTime date;

    @Schema(
            description = "Name of point"
    )
    private String pointName;
    @Schema(
            description = "point Id"
    )
    private Long pointId;

    @Schema(
            description = "Note content"
    )
    private String content;

    @Schema(
            description = "Authors username"
    )
    private String author;

}
