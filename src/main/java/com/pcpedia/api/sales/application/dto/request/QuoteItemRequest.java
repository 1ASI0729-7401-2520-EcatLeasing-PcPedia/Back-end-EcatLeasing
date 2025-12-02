package com.pcpedia.api.sales.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteItemRequest {

    @NotNull(message = "{validation.equipment.required}")
    private Long equipmentId;

    @NotNull(message = "{validation.quantity.required}")
    @Min(value = 1, message = "{validation.quantity.min}")
    private Integer quantity;

    @NotNull(message = "{validation.price.required}")
    @DecimalMin(value = "0.01", message = "{validation.price.min}")
    private BigDecimal unitPrice;
}
