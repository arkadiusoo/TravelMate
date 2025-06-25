package pl.sumatywny.travelmate.budget.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import pl.sumatywny.travelmate.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/trips/{tripId}/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Operations related to trip expenses: creating, listing, deleting, and summarizing trip-related financial data.")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService; // ✅ NEW: Add UserService

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
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"), // ✅ NEW
            @ApiResponse(responseCode = "404", description = "Trip or participant not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(
            @Parameter(description = "ID of the trip to add the expense to", required = true)
            @PathVariable UUID tripId,
            @Valid @RequestBody ExpenseDTO expenseDTO,
            Authentication authentication // ✅ NEW: Get current user
    ) {
        // ✅ NEW: Get current user ID
        UUID currentUserId = userService.getCurrentUserId(authentication);

        expenseDTO.setTripId(tripId);
        return ResponseEntity.ok(expenseService.addExpense(expenseDTO, currentUserId));
    }

    @Operation(
            summary = "Delete an expense",
            description = "Removes an expense from the system using its unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"), // ✅ NEW
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "ID of the expense to delete", required = true)
            @PathVariable UUID expenseId,
            Authentication authentication // ✅ NEW: Get current user
    ) {
        // ✅ NEW: Get current user ID and check permissions
        UUID currentUserId = userService.getCurrentUserId(authentication);
        expenseService.deleteExpense(expenseId, currentUserId);
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

    @Operation(
            summary = "Update an existing expense",
            description = "Replaces the entire expense entry with new data. All fields must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"), // ✅ NEW
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @Parameter(description = "ID of the expense to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseDTO dto,
            Authentication authentication // ✅ NEW: Get current user
    ) {
        // ✅ NEW: Get current user ID and check permissions
        UUID currentUserId = userService.getCurrentUserId(authentication);
        return ResponseEntity.ok(expenseService.updateExpense(id, dto, currentUserId));
    }

    @Operation(
            summary = "Partially update an expense",
            description = "Updates selected fields of an existing expense. Only fields included in the request body will be modified."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or unsupported field(s) in request", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"), // ✅ NEW
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> patchExpense(
            @Parameter(description = "ID of the expense to patch", required = true)
            @PathVariable UUID expenseId,
            @RequestBody Map<String, Object> updates,
            Authentication authentication // ✅ NEW: Get current user
    ) {
        // ✅ NEW: Get current user ID and check permissions
        UUID currentUserId = userService.getCurrentUserId(authentication);
        ExpenseDTO updated = expenseService.patchExpense(expenseId, updates, currentUserId);
        return ResponseEntity.ok(updated);
    }
}