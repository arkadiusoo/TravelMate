package pl.sumatywny.travelmate.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing a single trip expense")
public class ExpenseDTO {

    @Schema(description = "Unique identifier of the expense", example = "f1a8e0a2-345b-4c99-99ab-bc3f2b97cd1f", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Name of expense", example = "Hotel")
    private String name;

    @Schema(description = "Identifier of the trip to which this expense belongs", example = "b7c308ff-4906-4c63-bc8a-27a3ac0aa8f3")
    private UUID tripId;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Amount of the expense", example = "125.50", minimum = "0.01")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Category of the expense", example = "FOOD")
    private ExpenseCategory category;

    @Schema(description = "Optional description of the expense", example = "Dinner at a local restaurant")
    private String description;

    @Schema(
            description = "List of participant names who share this expense",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> participantNames;
    @NotNull
    @Schema(description = "Date when the expense occurred", example = "2025-04-20")
    private LocalDate date;

    @NotNull
    @Schema(description = "ID of the user who paid for the expense", example = "e8c40d9a-11e2-47cb-90fc-1c6d5bd6b0ae")
    private UUID payerId;

    @Schema(
        description = "Map of participants and their share in the expense. " +
                      "The sum of all shares should equal 1.0 (100%)",
        example = "{\"e8c4...\": 0.7, \"f3c6...\": 0.3}"
    )
    @NotEmpty
    private Map<UUID, BigDecimal> participantShares;
}