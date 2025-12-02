package com.pcpedia.api.sales.domain.model.aggregate;

import com.pcpedia.api.sales.domain.model.entity.ContractItem;
import com.pcpedia.api.sales.domain.model.enums.ContractStatus;
import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id")
    private Long quoteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contract_number", unique = true, nullable = false, length = 20)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthly_amount", precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractItem> items = new ArrayList<>();

    // Domain methods
    public void addItem(ContractItem item) {
        items.add(item);
        item.setContract(this);
    }

    public void cancel() {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Only active contracts can be cancelled");
        }
        this.status = ContractStatus.CANCELLED;
    }

    public void expire() {
        this.status = ContractStatus.EXPIRED;
    }

    public void renew() {
        this.status = ContractStatus.RENEWED;
    }

    public boolean isActive() {
        return this.status == ContractStatus.ACTIVE;
    }

    public boolean isExpired() {
        return this.endDate.isBefore(LocalDate.now());
    }

    public static String generateContractNumber() {
        return "CTR-" + LocalDate.now().getYear() + "-" +
                String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
