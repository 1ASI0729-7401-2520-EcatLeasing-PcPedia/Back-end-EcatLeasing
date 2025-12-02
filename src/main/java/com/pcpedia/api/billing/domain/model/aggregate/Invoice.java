package com.pcpedia.api.billing.domain.model.aggregate;

import com.pcpedia.api.billing.domain.model.enums.InvoiceStatus;
import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 20)
    private String invoiceNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Domain methods
    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }

    public void markAsOverdue() {
        if (this.dueDate.isBefore(LocalDate.now()) && this.status == InvoiceStatus.PENDING) {
            this.status = InvoiceStatus.OVERDUE;
        }
    }

    public void cancel() {
        this.status = InvoiceStatus.CANCELLED;
    }

    public boolean isPending() {
        return this.status == InvoiceStatus.PENDING || this.status == InvoiceStatus.OVERDUE;
    }

    public static String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" +
                String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
