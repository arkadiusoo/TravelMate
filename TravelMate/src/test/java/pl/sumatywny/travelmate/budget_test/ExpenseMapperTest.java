package pl.sumatywny.travelmate.budget_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.Expense;
import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.budget.service.ExpenseMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseMapperTest {

    private ExpenseMapper expenseMapper;
    private UUID id;
    private UUID tripId;
    private UUID payerId;
    private Map<UUID, BigDecimal> shares;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        expenseMapper = new ExpenseMapper();
        id = UUID.randomUUID();
        tripId = UUID.randomUUID();
        payerId = UUID.randomUUID();
        shares = Map.of(payerId, BigDecimal.ONE);
        date = LocalDate.now();
    }

    @Test
    void shouldMapDtoToEntity() {
        ExpenseDTO dto = ExpenseDTO.builder()
                .id(id)
                .tripId(tripId)
                .amount(BigDecimal.TEN)
                .category(ExpenseCategory.FOOD)
                .description("Test lunch")
                .date(date)
                .payerId(payerId)
                .participantShares(shares)
                .build();

        Expense entity = expenseMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getTripId()).isEqualTo(dto.getTripId());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getCategory()).isEqualTo(dto.getCategory());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getDate()).isEqualTo(dto.getDate());
        assertThat(entity.getPayerId()).isEqualTo(dto.getPayerId());
        assertThat(entity.getParticipantShares()).isEqualTo(dto.getParticipantShares());
    }

    @Test
    void shouldMapEntityToDto() {
        Expense entity = Expense.builder()
                .id(id)
                .tripId(tripId)
                .amount(BigDecimal.TEN)
                .category(ExpenseCategory.ACCOMMODATION)
                .description("Hotel")
                .date(date)
                .payerId(payerId)
                .participantShares(shares)
                .build();

        ExpenseDTO dto = expenseMapper.toDTO(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getTripId()).isEqualTo(entity.getTripId());
        assertThat(dto.getAmount()).isEqualTo(entity.getAmount());
        assertThat(dto.getCategory()).isEqualTo(entity.getCategory());
        assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
        assertThat(dto.getDate()).isEqualTo(entity.getDate());
        assertThat(dto.getPayerId()).isEqualTo(entity.getPayerId());
        assertThat(dto.getParticipantShares()).isEqualTo(entity.getParticipantShares());
    }

    @Test
    void shouldReturnNullWhenDtoIsNull() {
        Expense entity = expenseMapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        ExpenseDTO dto = expenseMapper.toDTO(null);
        assertThat(dto).isNull();
    }
}