package pl.sumatywny.travelmate.budget.service;

import org.springframework.stereotype.Component;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.Expense;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseDTO dto) {
        if (dto == null) return null;

        return Expense.builder()
                .id(dto.getId())
                .name(dto.getName())
                .tripId(dto.getTripId())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .date(dto.getDate())
                .payerId(dto.getPayerId())
                .participantShares(dto.getParticipantShares())
                .build();
    }

    public ExpenseDTO toDTO(Expense entity) {
        if (entity == null) return null;

        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .tripId(entity.getTripId())
                .amount(entity.getAmount())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .date(entity.getDate())
                .payerId(entity.getPayerId())
                .participantShares(entity.getParticipantShares())
                .build();
    }
}