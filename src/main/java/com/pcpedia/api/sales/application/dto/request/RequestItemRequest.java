package com.pcpedia.api.sales.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemRequest {

    @NotNull(message = "{validation.productModel.required}")
    private Long productModelId;

    @NotNull(message = "{validation.quantity.required}")
    @Min(value = 1, message = "{validation.quantity.min}")
    private Integer quantity;
}
