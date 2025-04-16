package pl.sumatywny.travelmate.budget.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tripId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private UUID payerId;

    @ElementCollection
    @CollectionTable(
        name = "expense_participants",
        joinColumns = @JoinColumn(name = "expense_id")
    )
    @Column(name = "participant_id", nullable = false)
    private List<UUID> participantIds;
}