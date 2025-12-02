package com.pcpedia.api.sales.domain.model.entity;

import com.pcpedia.api.sales.domain.model.aggregate.Quote;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
