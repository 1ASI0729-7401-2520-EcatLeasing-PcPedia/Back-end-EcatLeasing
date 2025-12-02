package com.pcpedia.api.inventory.domain.model.aggregate;

import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModel extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "productModel", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Equipment> equipments = new ArrayList<>();

    // Computed: count equipment by status
    public long getAvailableCount() {
        return equipments.stream()
                .filter(e -> e.getStatus().name().equals("AVAILABLE"))
                .count();
    }

    public long getLeasedCount() {
        return equipments.stream()
                .filter(e -> e.getStatus().name().equals("LEASED"))
                .count();
    }

    public long getTotalCount() {
        return equipments.size();
    }
}
