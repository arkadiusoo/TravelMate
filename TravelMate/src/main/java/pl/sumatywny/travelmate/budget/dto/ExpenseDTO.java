package pl.sumatywny.travelmate.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Lombok annotations
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private UUID id;
    private UUID tripId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private ExpenseCategory category;
    private String description;

    @NotNull
    private LocalDate date;

    @NotNull
    private UUID payerId;

    @NotEmpty
    private List<UUID> participantIds;
}