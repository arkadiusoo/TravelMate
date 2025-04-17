package pl.sumatywny.travelmate.budget.service;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.config.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.Expense;
import pl.sumatywny.travelmate.budget.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public List<ExpenseDTO> getExpensesByTrip(UUID tripId) {
        return expenseRepository.findAllByTripId(tripId)
                .stream()
                .map(expenseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        Expense expense = expenseMapper.toEntity(expenseDTO);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    public void deleteExpense(UUID id) {
        expenseRepository.deleteById(id);
    }

    public BudgetSummaryDTO getBudgetSummary(UUID tripId) {
    List<Expense> expenses = expenseRepository.findAllByTripId(tripId);

    BigDecimal total = BigDecimal.ZERO;
    Map<UUID, BigDecimal> paid = new HashMap<>();
    Map<UUID, BigDecimal> share = new HashMap<>();

    for (Expense expense : expenses) {
        total = total.add(expense.getAmount());

        paid.merge(expense.getPayerId(), expense.getAmount(), BigDecimal::add);

        BigDecimal part = expense.getAmount()
                .divide(BigDecimal.valueOf(expense.getParticipantIds().size()), 2, RoundingMode.HALF_UP);

        for (UUID participant : expense.getParticipantIds()) {
            share.merge(participant, part, BigDecimal::add);
        }
    }

    Map<UUID, BigDecimal> balance = new HashMap<>();
    for (UUID participant : share.keySet()) {
        BigDecimal paidAmount = paid.getOrDefault(participant, BigDecimal.ZERO);
        BigDecimal shareAmount = share.get(participant);
        balance.put(participant, paidAmount.subtract(shareAmount));
    }

    return new BudgetSummaryDTO(total, share, paid, balance);
}

    public ExpenseDTO updateExpense(UUID id, ExpenseDTO dto) {
        Expense existing = expenseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Expense not found"));

        existing.setAmount(dto.getAmount());
        existing.setCategory(dto.getCategory());
        existing.setDate(dto.getDate());
        existing.setDescription(dto.getDescription());
        existing.setPayerId(dto.getPayerId());
        existing.setParticipantIds(dto.getParticipantIds());

        return expenseMapper.toDTO(expenseRepository.save(existing));
    }

    public ExpenseDTO patchExpense(UUID id, Map<String, Object> updates) {
    Expense expense = expenseRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Expense not found"));

    updates.forEach((key, value) -> {
        switch (key) {
            case "amount" -> expense.setAmount(new BigDecimal(value.toString()));
            case "category" -> expense.setCategory(ExpenseCategory.valueOf(value.toString()));
            case "description" -> expense.setDescription(value.toString());
            case "date" -> expense.setDate(LocalDate.parse(value.toString()));
            case "payerId" -> expense.setPayerId(UUID.fromString(value.toString()));
            case "participantIds" -> {
                @SuppressWarnings("unchecked")
                List<String> rawList = (List<String>) value;
                List<UUID> participantIds = rawList.stream()
                        .map(UUID::fromString)
                        .toList();
                expense.setParticipantIds(participantIds);
            }
        }
    });

    Expense saved = expenseRepository.save(expense);
    return expenseMapper.toDTO(saved);
}
}