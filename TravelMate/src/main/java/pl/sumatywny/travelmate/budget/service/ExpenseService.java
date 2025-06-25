package pl.sumatywny.travelmate.budget.service;

import pl.sumatywny.travelmate.budget.model.ExpenseCategory;
import pl.sumatywny.travelmate.config.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sumatywny.travelmate.budget.dto.BudgetSummaryDTO;
import pl.sumatywny.travelmate.budget.dto.ExpenseDTO;
import pl.sumatywny.travelmate.budget.model.Expense;
import pl.sumatywny.travelmate.budget.repository.ExpenseRepository;
import pl.sumatywny.travelmate.security.model.User;
import pl.sumatywny.travelmate.security.service.UserService;
import pl.sumatywny.travelmate.participant.service.TripPermissionService;
import pl.sumatywny.travelmate.participant.model.ParticipantRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UserService userService;
    private final TripPermissionService permissionService;

    public List<ExpenseDTO> getExpensesByTrip(UUID tripId) {
        return expenseRepository.findAllByTripId(tripId)
                .stream()
                .map(this::enhanceWithParticipantNames)
                .toList();
    }

    private ExpenseDTO enhanceWithParticipantNames(Expense expense) {
        ExpenseDTO dto = expenseMapper.toDTO(expense);

        List<String> participantNames = new ArrayList<>();
        for (UUID userId : expense.getParticipantShares().keySet()) {
            try {
                User user = userService.findById(userId);
                if (user != null) {
                    String name = getDisplayName(user);
                    participantNames.add(name);
                }
            } catch (Exception e) {
                participantNames.add("Unknown");
            }
        }

        dto.setParticipantNames(participantNames);
        return dto;
    }

    private String getDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null &&
                !user.getFirstName().trim().isEmpty() && !user.getLastName().trim().isEmpty()) {
            return user.getFirstName() + " " + user.getLastName();
        }

        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            return user.getFirstName();
        }

        if (user.getEmail() != null) {
            return user.getEmail().split("@")[0];
        }

        return "Unknown User";
    }

    // ✅ PROSTA logika - GUEST nie może nic
    private void checkNotGuest(UUID tripId, UUID currentUserId, String action) {
        ParticipantRole userRole = permissionService.getUserRole(tripId, currentUserId);
        if (userRole == null || userRole == ParticipantRole.GUEST) {
            throw new IllegalStateException("Nie masz uprawnień do " + action + ". Goście mają dostęp tylko do odczytu.");
        }
    }

    // ✅ PROSTA logika - tylko ORGANIZER może usuwać
    private void checkIsOrganizer(UUID tripId, UUID currentUserId, String action) {
        ParticipantRole userRole = permissionService.getUserRole(tripId, currentUserId);
        if (userRole != ParticipantRole.ORGANIZER) {
            throw new IllegalStateException("Nie masz uprawnień do " + action + ". Tylko organizatorzy mogą to robić.");
        }
    }

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO, UUID currentUserId) {
        // ✅ MEMBER i ORGANIZER mogą dodawać
        checkNotGuest(expenseDTO.getTripId(), currentUserId, "dodawania wydatków");

        BigDecimal totalShare = expenseDTO.getParticipantShares().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShare.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Sum of participant shares must equal 1.0");
        }

        Expense expense = expenseMapper.toEntity(expenseDTO);
        Expense saved = expenseRepository.save(expense);
        return enhanceWithParticipantNames(saved);
    }

    public void deleteExpense(UUID id, UUID currentUserId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        // ✅ Tylko ORGANIZER może usuwać
        checkIsOrganizer(expense.getTripId(), currentUserId, "usuwania wydatków");

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

            for (Map.Entry<UUID, BigDecimal> entry : expense.getParticipantShares().entrySet()) {
                UUID participant = entry.getKey();
                BigDecimal participantShare = expense.getAmount().multiply(entry.getValue());
                share.merge(participant, participantShare, BigDecimal::add);
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

    public ExpenseDTO updateExpense(UUID id, ExpenseDTO dto, UUID currentUserId) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        // ✅ MEMBER i ORGANIZER mogą edytować
        checkNotGuest(existing.getTripId(), currentUserId, "edytowania wydatków");

        existing.setAmount(dto.getAmount());
        existing.setCategory(dto.getCategory());
        existing.setDate(dto.getDate());
        existing.setDescription(dto.getDescription());
        existing.setPayerId(dto.getPayerId());
        existing.setParticipantShares(dto.getParticipantShares());

        return enhanceWithParticipantNames(expenseRepository.save(existing));
    }

    public ExpenseDTO patchExpense(UUID id, Map<String, Object> updates, UUID currentUserId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        // ✅ MEMBER i ORGANIZER mogą edytować
        checkNotGuest(expense.getTripId(), currentUserId, "edytowania wydatków");

        updates.forEach((key, value) -> {
            switch (key) {
                case "amount" -> expense.setAmount(new BigDecimal(value.toString()));
                case "category" -> expense.setCategory(ExpenseCategory.valueOf(value.toString()));
                case "description" -> expense.setDescription(value.toString());
                case "date" -> expense.setDate(LocalDate.parse(value.toString()));
                case "payerId" -> expense.setPayerId(UUID.fromString(value.toString()));
                case "participantShares" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rawMap = (Map<String, Object>) value;

                    Map<UUID, BigDecimal> parsed = new HashMap<>();
                    for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                        parsed.put(UUID.fromString(entry.getKey()), new BigDecimal(entry.getValue().toString()));
                    }

                    BigDecimal sum = parsed.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (sum.compareTo(BigDecimal.ONE) != 0) {
                        throw new IllegalArgumentException("Sum of participant shares must equal 1.0");
                    }

                    expense.setParticipantShares(parsed);
                }
            }
        });

        return enhanceWithParticipantNames(expenseRepository.save(expense));
    }
}