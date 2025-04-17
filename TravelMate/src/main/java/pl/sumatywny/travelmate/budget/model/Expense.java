package pl.sumatywny.travelmate.budget.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    @CollectionTable(name = "expense_participant_shares", joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "participant_id")
    @Column(name = "share", nullable = false)
    private Map<UUID, BigDecimal> participantShares;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}