package pl.sumatywny.travelmate.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryDTO {
    private BigDecimal totalTripCost;               // total cost of the trip
    private Map<UUID, BigDecimal> participantShare; // how much each participant should pay
    private Map<UUID, BigDecimal> actualPaid;       // how much each participant actually paid
    private Map<UUID, BigDecimal> balance;          // balance: overpayment or underpayment
}
