package pl.sumatywny.travelmate.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sumatywny.travelmate.budget.model.Expense;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findAllByTripId(UUID tripId);

    // already implemented due to JpaReposiotry:
    //save(S entity)
    //saveAll(Iterable<S>)
    //findById(ID id)
    //existsById(ID id)
    //findAll()
    //findAllById(Iterable<ID>)
    //count()
    //deleteById(ID id)
    //delete(Expense entity)
    //deleteAll()
    //deleteAll(Iterable<Expense>)
}