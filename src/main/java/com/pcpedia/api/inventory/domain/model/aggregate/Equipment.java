package com.pcpedia.api.inventory.domain.model.aggregate;

import com.pcpedia.api.inventory.domain.model.enums.EquipmentStatus;
import com.pcpedia.api.shared.domain.model.AggregateRoot;
import com.pcpedia.api.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends AuditableEntity implements AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_model_id")
    private ProductModel productModel;

    @Column(length = 150)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    public String getEffectiveName() {
        if (name != null && !name.isEmpty()) return name;
        return productModel != null ? productModel.getName() : null;
    }

    public String getEffectiveBrand() {
        if (brand != null && !brand.isEmpty()) return brand;
        return productModel != null ? productModel.getBrand() : null;
    }

    public String getEffectiveModel() {
        if (model != null && !model.isEmpty()) return model;
        return productModel != null ? productModel.getModel() : null;
    }

    public String getEffectiveCategory() {
        if (category != null && !category.isEmpty()) return category;
        return productModel != null ? productModel.getCategory() : null;
    }

    public String getEffectiveSpecifications() {
        if (specifications != null && !specifications.isEmpty()) return specifications;
        return productModel != null ? productModel.getSpecifications() : null;
    }

    public BigDecimal getEffectiveBasePrice() {
        if (basePrice != null) return basePrice;
        return productModel != null ? productModel.getBasePrice() : null;
    }

    public String getEffectiveImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        return productModel != null ? productModel.getImageUrl() : null;
    }

    public boolean isAvailable() {
        return this.status == EquipmentStatus.AVAILABLE;
    }

    public void markAsLeased() {
        if (!isAvailable()) {
            throw new IllegalStateException("Equipment is not available for leasing");
        }
        this.status = EquipmentStatus.LEASED;
    }

    public void markAsAvailable() {
        this.status = EquipmentStatus.AVAILABLE;
    }

    public void markAsMaintenance() {
        this.status = EquipmentStatus.MAINTENANCE;
    }

    public void retire() {
        this.status = EquipmentStatus.RETIRED;
    }
}
