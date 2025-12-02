package com.pcpedia.api.sales.domain.model.entity;

import com.pcpedia.api.sales.domain.model.aggregate.Request;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(name = "product_model_id", nullable = false)
    private Long productModelId;

    @Column(nullable = false)
    private Integer quantity;
}
