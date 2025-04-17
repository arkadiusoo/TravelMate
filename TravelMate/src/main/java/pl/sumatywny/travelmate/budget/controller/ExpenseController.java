package pl.sumatywny.travelmate.budget.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Operations related to trip expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get all expenses for a trip")
    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(@PathVariable UUID tripId) {
        return ResponseEntity.ok(expenseService.getExpensesByTrip(tripId));

    }

    @Operation(summary = "Add a new expense to a trip")
    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(
            @PathVariable UUID tripId,
            @Valid @RequestBody ExpenseDTO expenseDTO) {
            expenseDTO.setTripId(tripId);
            return ResponseEntity.ok(expenseService.addExpense(expenseDTO));
    }

    @Operation(summary = "Delete an expense")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
    summary = "Get budget summary for a trip",
    description = "Returns a financial summary of the trip including total cost, " +
                  "each participant's expected share, actual payments, and balance."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Budget summary retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Trip not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
@GetMapping("/budget-summary")
public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(
    @Parameter(description = "ID of the trip to get the summary for", required = true)
    @PathVariable UUID tripId
) {
    return ResponseEntity.ok(expenseService.getBudgetSummary(tripId));
}
}