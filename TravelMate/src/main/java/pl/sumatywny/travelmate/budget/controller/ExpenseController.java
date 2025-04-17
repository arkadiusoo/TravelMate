package pl.sumatywny.travelmate.budget.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "Expenses", description = "Operations related to trip expenses: creating, listing, deleting, and summarizing trip-related financial data.")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
        summary = "Get all expenses for a trip",
        description = "Returns a list of all expenses recorded for a given trip, including amount, category, participants, payer, and date."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
        @Parameter(description = "ID of the trip to retrieve expenses for", required = true)
        @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByTrip(tripId));
    }

    @Operation(
        summary = "Add a new expense to a trip",
        description = "Adds a new expense for a specific trip. You must provide the amount, category, payer, and participants involved."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "404", description = "Trip or participant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(
        @Parameter(description = "ID of the trip to add the expense to", required = true)
        @PathVariable UUID tripId,
        @Valid @RequestBody ExpenseDTO expenseDTO
    ) {
        expenseDTO.setTripId(tripId);
        return ResponseEntity.ok(expenseService.addExpense(expenseDTO));
    }

    @Operation(
        summary = "Delete an expense",
        description = "Removes an expense from the system using its unique ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
        @Parameter(description = "ID of the expense to delete", required = true)
        @PathVariable UUID expenseId
    ) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get budget summary for a trip",
        description = "Returns a financial summary of the trip including total expenses, individual shares, actual payments, and balance per participant."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budget summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/budget-summary")
    public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(
        @Parameter(description = "ID of the trip to get the budget summary for", required = true)
        @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(expenseService.getBudgetSummary(tripId));
    }
}