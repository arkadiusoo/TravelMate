package pl.sumatywny.travelmate.budget.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<ExpenseDTO> addExpense(@PathVariable UUID tripId, @RequestBody ExpenseDTO expenseDTO) {
        expenseDTO.setTripId(tripId);
        return ResponseEntity.ok(expenseService.addExpense(expenseDTO));
    }

    @Operation(summary = "Delete an expense")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}