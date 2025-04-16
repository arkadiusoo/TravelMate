package pl.sumatywny.travelmate.budget.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sumatywny.travelmate.budget.model.Expense;
import pl.sumatywny.travelmate.budget.service.ExpenseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(@PathVariable UUID tripId) {
        return ResponseEntity.ok(expenseService.getExpensesByTrip(tripId));
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(@PathVariable UUID tripId, @RequestBody Expense expense) {
        expense.setTripId(tripId); // przypisujemy tripId z path variable
        return ResponseEntity.ok(expenseService.addExpense(expense));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}