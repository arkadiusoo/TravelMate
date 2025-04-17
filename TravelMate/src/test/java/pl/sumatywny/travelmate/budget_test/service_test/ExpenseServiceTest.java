package pl.sumatywny.travelmate.budget_test.service_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.Expense;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.repository.ExpenseRepository;
import pl.sumatywny.travelmate.budget.service.ExpenseMapper;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import pl.sumatywny.travelmate.config.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseService expenseService;

    private UUID tripId;
    private UUID expenseId;
    private UUID userId;
    private Expense expense;
    private ExpenseDTO expenseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tripId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        Map<UUID, BigDecimal> shares = new HashMap<>();
        shares.put(userId, BigDecimal.ONE);

        expense = Expense.builder()
                .id(expenseId)
                .tripId(tripId)
                .amount(BigDecimal.valueOf(100))
                .category(ExpenseCategory.FOOD)
                .description("Lunch")
                .date(LocalDate.now())
                .payerId(userId)
                .participantShares(shares)
                .build();

        expenseDTO = ExpenseDTO.builder()
                .id(expenseId)
                .tripId(tripId)
                .amount(BigDecimal.valueOf(100))
                .category(ExpenseCategory.FOOD)
                .description("Lunch")
                .date(LocalDate.now())
                .payerId(userId)
                .participantShares(shares)
                .build();
    }

    @Test
    void shouldReturnExpensesForTrip() {
        when(expenseRepository.findAllByTripId(tripId)).thenReturn(List.of(expense));
        when(expenseMapper.toDTO(expense)).thenReturn(expenseDTO);

        List<ExpenseDTO> result = expenseService.getExpensesByTrip(tripId);

        assertThat(result).containsExactly(expenseDTO);
    }

    @Test
    void shouldAddExpenseWithValidShares() {
        when(expenseMapper.toEntity(expenseDTO)).thenReturn(expense);
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toDTO(expense)).thenReturn(expenseDTO);

        ExpenseDTO result = expenseService.addExpense(expenseDTO);

        assertThat(result).isEqualTo(expenseDTO);
    }

    @Test
    void shouldThrowWhenSharesNotEqualOne() {
        Map<UUID, BigDecimal> badShares = new HashMap<>();
        badShares.put(userId, BigDecimal.valueOf(0.5));
        expenseDTO.setParticipantShares(badShares);

        assertThatThrownBy(() -> expenseService.addExpense(expenseDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sum of participant shares must equal 1.0");
    }

    @Test
    void shouldDeleteExpenseById() {
        UUID id = UUID.randomUUID();

        expenseService.deleteExpense(id);

        verify(expenseRepository).deleteById(id);
    }

    @Test
    void shouldReturnBudgetSummary() {
        when(expenseRepository.findAllByTripId(tripId)).thenReturn(List.of(expense));

        BudgetSummaryDTO result = expenseService.getBudgetSummary(tripId);

        assertThat(result.getTotalTripCost()).isEqualByComparingTo("100.00");
        assertThat(result.getParticipantShare()).containsEntry(userId, new BigDecimal("100.00"));
        assertThat(result.getActualPaid()).containsEntry(userId, new BigDecimal("100.00"));
        assertThat(result.getBalance()).containsEntry(userId, BigDecimal.ZERO);
    }

    @Test
    void shouldUpdateExpense() {
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toDTO(expense)).thenReturn(expenseDTO);

        ExpenseDTO updated = expenseService.updateExpense(expenseId, expenseDTO);

        assertThat(updated).isEqualTo(expenseDTO);
    }

    @Test
    void shouldPatchExpenseFields() {
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any())).thenReturn(expense);
        when(expenseMapper.toDTO(expense)).thenReturn(expenseDTO);

        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Updated Description");
        updates.put("participantShares", Map.of(userId.toString(), 1.0));

        ExpenseDTO result = expenseService.patchExpense(expenseId, updates);

        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void shouldThrowWhenPatchingWithBadShares() {
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        Map<String, Object> updates = Map.of(
                "participantShares", Map.of(userId.toString(), 0.5)
        );

        assertThatThrownBy(() -> expenseService.patchExpense(expenseId, updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sum of participant shares must equal 1.0");
    }

    @Test
    void shouldThrowWhenExpenseNotFound() {
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(expenseId, expenseDTO))
                .isInstanceOf(NotFoundException.class);
    }
}
