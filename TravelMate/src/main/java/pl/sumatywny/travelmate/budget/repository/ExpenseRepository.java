package pl.sumatywny.travelmate.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.budget.model.Expense;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findAllByTripId(UUID tripId);
}