package com.pcpedia.api.sales.domain.model.aggregate;

import com.pcpedia.api.sales.domain.model.entity.QuoteItem;
import com.pcpedia.api.sales.domain.model.enums.QuoteStatus;
import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "total_monthly", precision = 10, scale = 2)
    private BigDecimal totalMonthly;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuoteItem> items = new ArrayList<>();

    // Domain methods
    public void addItem(QuoteItem item) {
        items.add(item);
        item.setQuote(this);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalMonthly = items.stream()
                .map(QuoteItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void send() {
        if (this.status != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only draft quotes can be sent");
        }
        this.status = QuoteStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void accept() {
        if (this.status != QuoteStatus.SENT) {
            throw new IllegalStateException("Only sent quotes can be accepted");
        }
        if (this.validUntil != null && this.validUntil.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Quote has expired");
        }
        this.status = QuoteStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != QuoteStatus.SENT) {
            throw new IllegalStateException("Only sent quotes can be rejected");
        }
        this.status = QuoteStatus.REJECTED;
    }

    public boolean isAccepted() {
        return this.status == QuoteStatus.ACCEPTED;
    }

    public boolean isDraft() {
        return this.status == QuoteStatus.DRAFT;
    }
}
