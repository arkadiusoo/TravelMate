package pl.sumatywny.travelmate.budget_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.budget.controller.ExpenseController;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.service.ExpenseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID tripId;
    private UUID expenseId;
    private UUID userId;
    private ExpenseDTO dto;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        dto = ExpenseDTO.builder()
                .id(expenseId)
                .tripId(tripId)
                .amount(BigDecimal.TEN)
                .category(ExpenseCategory.FOOD)
                .description("Test")
                .date(LocalDate.now())
                .payerId(userId)
                .participantShares(Map.of(userId, BigDecimal.ONE))
                .build();
    }

    @Test
    void shouldGetExpenses() throws Exception {
        when(expenseService.getExpensesByTrip(tripId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/trips/{tripId}/expenses", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void shouldAddExpense() throws Exception {
        when(expenseService.addExpense(any())).thenReturn(dto);

        mockMvc.perform(post("/trips/{tripId}/expenses", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId().toString()));
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        doNothing().when(expenseService).deleteExpense(expenseId);

        mockMvc.perform(delete("/trips/{tripId}/expenses/{expenseId}", tripId, expenseId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBudgetSummary() throws Exception {
        BudgetSummaryDTO summary = new BudgetSummaryDTO(BigDecimal.TEN,
                Map.of(userId, BigDecimal.TEN),
                Map.of(userId, BigDecimal.TEN),
                Map.of(userId, BigDecimal.ZERO));

        when(expenseService.getBudgetSummary(tripId)).thenReturn(summary);

        mockMvc.perform(get("/trips/{tripId}/expenses/budget-summary", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTripCost").value(10));
    }

    @Test
    void shouldUpdateExpense() throws Exception {
        when(expenseService.updateExpense(eq(expenseId), any())).thenReturn(dto);

        mockMvc.perform(put("/trips/{tripId}/expenses/{id}", tripId, expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId().toString()));
    }

    @Test
    void shouldPatchExpense() throws Exception {
        Map<String, Object> updates = Map.of("description", "Updated desc");
        when(expenseService.patchExpense(eq(expenseId), any())).thenReturn(dto);

        mockMvc.perform(patch("/trips/{tripId}/expenses/{expenseId}", tripId, expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId().toString()));
    }
}
