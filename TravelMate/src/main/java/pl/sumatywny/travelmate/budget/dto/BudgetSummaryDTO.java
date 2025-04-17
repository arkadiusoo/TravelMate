package pl.sumatywny.travelmate.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Summary of a trip's budget including total cost, individual shares, actual payments, and balances")
public class BudgetSummaryDTO {

    @Schema(
        description = "Total cost of all expenses for the trip",
        example = "320.50"
    )
    private BigDecimal totalTripCost;

    @Schema(
        description = "Map of participant IDs and the amount each one should pay",
        example = "{ \"a1b2c3d4-5678-90ab-cdef-1234567890ab\": 100.25, \"b2c3d4e5-6789-01bc-def1-2345678901bc\": 220.25 }"
    )
    private Map<UUID, BigDecimal> participantShare;

    @Schema(
        description = "Map of participant IDs and the actual amount paid by each one",
        example = "{ \"a1b2c3d4-5678-90ab-cdef-1234567890ab\": 150.00, \"b2c3d4e5-6789-01bc-def1-2345678901bc\": 170.50 }"
    )
    private Map<UUID, BigDecimal> actualPaid;

    @Schema(
        description = "Map of participant IDs and their balance (positive = overpaid, negative = underpaid)",
        example = "{ \"a1b2c3d4-5678-90ab-cdef-1234567890ab\": 49.75, \"b2c3d4e5-6789-01bc-def1-2345678901bc\": -49.75 }"
    )
    private Map<UUID, BigDecimal> balance;
}