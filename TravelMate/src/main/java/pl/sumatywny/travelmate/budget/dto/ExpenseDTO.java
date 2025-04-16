package pl.sumatywny.travelmate.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private UUID id;
    private UUID tripId;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String description;
    private LocalDate date;
    private UUID payerId;
    private List<UUID> participantIds;
}