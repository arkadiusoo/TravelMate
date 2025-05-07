package pl.sumatywny.travelmate.participant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sumatywny.travelmate.participant.model.InvitationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for responding to a trip invitation")
public class InvitationResponseDTO {

    @NotNull
    @Schema(description = "Response to the invitation (ACCEPTED or DECLINED)", example = "ACCEPTED")
    private InvitationStatus status;
}