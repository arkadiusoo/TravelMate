package pl.sumatywny.travelmate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import pl.sumatywny.travelmate.budget.controller.ExpenseController;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.service.ExpenseService;
import pl.sumatywny.travelmate.security.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private UserService userService;

    private UUID tripId;
    private UUID expenseId;
    private UUID payerId;
    private UUID currentUserId;
    private ExpenseDTO sampleExpense;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        payerId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();

        // Stub current user lookup
        when(userService.getCurrentUserId(any(Authentication.class))).thenReturn(currentUserId);

        // Przygotuj przykładowe ExpenseDTO
        sampleExpense = ExpenseDTO.builder()
            .id(expenseId)
            .tripId(tripId)
            .name("Lunch")
            .amount(new BigDecimal("10.00"))
            .category(ExpenseCategory.FOOD)
            .description("Business lunch")
            .date(LocalDate.of(2025, 6, 29))
            .payerId(payerId)
            .participantShares(Map.of(payerId, new BigDecimal("1.0")))
            .participantPaymentStatus(Map.of(payerId, true))
            .participantNames(List.of("Alice"))
            .build();
    }

    @Test
    void testGetExpenses() throws Exception {
        when(expenseService.getExpensesByTrip(tripId)).thenReturn(List.of(sampleExpense));

        mockMvc.perform(get("/api/trips/{tripId}/expenses", tripId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(expenseId.toString()))
            .andExpect(jsonPath("$[0].name").value("Lunch"));
    }

    @Test
    void testAddExpense() throws Exception {
        ExpenseDTO requestDto = ExpenseDTO.builder()
            .name("Lunch")
            .amount(new BigDecimal("10.00"))
            .category(ExpenseCategory.FOOD)
            .description("Business lunch")
            .date(LocalDate.of(2025, 6, 29))
            .payerId(payerId)
            .participantShares(Map.of(payerId, new BigDecimal("1.0")))
            .participantPaymentStatus(Map.of(payerId, true))
            .participantNames(List.of("Alice"))
            .build();

        when(expenseService.addExpense(any(ExpenseDTO.class), eq(currentUserId)))
            .thenReturn(sampleExpense);

        mockMvc.perform(post("/api/trips/{tripId}/expenses", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expenseId.toString()))
            .andExpect(jsonPath("$.tripId").value(tripId.toString()))
            .andExpect(jsonPath("$.name").value("Lunch"));
    }

    @Test
    void testUpdateExpense() throws Exception {
        when(expenseService.updateExpense(eq(expenseId), any(ExpenseDTO.class), eq(currentUserId)))
            .thenReturn(sampleExpense);

        mockMvc.perform(put("/api/trips/{tripId}/expenses/{id}", tripId, expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleExpense)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expenseId.toString()))
            .andExpect(jsonPath("$.description").value("Business lunch"));
    }

    @Test
    void testPatchExpense() throws Exception {
        Map<String, Object> updates = Map.of("description", "Updated lunch");

        // Zamiast toBuilder(), budujemy nowy obiekt
        ExpenseDTO patched = ExpenseDTO.builder()
            .id(sampleExpense.getId())
            .tripId(sampleExpense.getTripId())
            .name(sampleExpense.getName())
            .amount(sampleExpense.getAmount())
            .category(sampleExpense.getCategory())
            .description("Updated lunch")
            .date(sampleExpense.getDate())
            .payerId(sampleExpense.getPayerId())
            .participantShares(sampleExpense.getParticipantShares())
            .participantPaymentStatus(sampleExpense.getParticipantPaymentStatus())
            .participantNames(sampleExpense.getParticipantNames())
            .build();

        when(expenseService.patchExpense(eq(expenseId), eq(updates), eq(currentUserId)))
            .thenReturn(patched);

        mockMvc.perform(patch("/api/trips/{tripId}/expenses/{expenseId}", tripId, expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated lunch"));
    }

    @Test
    void testDeleteExpense() throws Exception {
        doNothing().when(expenseService).deleteExpense(expenseId, currentUserId);

        mockMvc.perform(delete("/api/trips/{tripId}/expenses/{expenseId}", tripId, expenseId))
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetBudgetSummary() throws Exception {
        BudgetSummaryDTO summary = BudgetSummaryDTO.builder()
            .totalTripCost(new BigDecimal("100.00"))
            .participantShare(Map.of(payerId, new BigDecimal("90.00")))
            .actualPaid(Map.of(payerId, new BigDecimal("10.00")))
            .balance(Map.of(payerId, new BigDecimal("80.00")))
            .build();

        when(expenseService.getBudgetSummary(tripId)).thenReturn(summary);

        mockMvc.perform(get("/api/trips/{tripId}/expenses/budget-summary", tripId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTripCost").value(100.00))
            .andExpect(jsonPath("$.participantShare.['" + payerId + "']").value(90.00))
            .andExpect(jsonPath("$.actualPaid.['" + payerId + "']").value(10.00))
            .andExpect(jsonPath("$.balance.['" + payerId + "']").value(80.00));
    }
}